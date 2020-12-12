import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class Chicken {
    public static void main(String[] args) {
        LocalDate nowDate = LocalDate.now(); // 当前日期

        //啤酒
        Beer b1 = new Beer("黄泉酒",999.99,nowDate,250);
        System.out.print(b1.toString());
        System.out.println(b1.name + (b1.isOverdue()?"过期":"没过期")+"\n");
        Beer b2 = new Beer("过期酒",999.99,LocalDate.of(2000,1,1),250);
        System.out.print(b2.toString());
        System.out.println(b2.name + (b2.isOverdue()?"过期":"没过期")+"\n");

        //果汁
        Juice j1 = new Juice("美汁汁",2.5,nowDate);
        System.out.print(j1.toString()+"\n");

        //炸鸡店
        West2FriedChickenRestauran w = new West2FriedChickenRestauran(1000.50);

        //尝试进货
        try {
            w.inputGoods(b1,1);
            w.inputGoods(j1,1);
        }catch (OverdraftBalanceException e) {
            System.out.println(e.getMessage());
        }

        //卖套餐
        w.sellMeal("阴间啤酒餐");

        //尝试进货
        try {
            w.inputGoods(j1,100);
        }catch (OverdraftBalanceException e) {
            System.out.println(e.getMessage());
        }

        //卖套餐
        w.sellMeal("美汁汁餐");
        w.sellMeal("儿童套餐");
        w.sellMeal("阴间啤酒餐");

        //尝试进货
        try {
            w.inputGoods(j1,10000);
        }catch (OverdraftBalanceException e) {
            System.out.println(e.getMessage());
        }

    }
}

abstract class Drinks {  //抽象饮料类
    protected String name; //名字
    protected double cost;//成本
    protected LocalDate produceDate;//生产日期
    protected int keepDay;//保质期

    public Drinks(String name,double cost,LocalDate produceDate,int keepDay) {
        this.name = name;
        this.cost = cost;
        this.produceDate = produceDate;
        this.keepDay = keepDay;
    }

    public boolean isOverdue() { //是否过期
        LocalDate nowDate = LocalDate.now(); // 当前日期
        LocalDate deadLine = produceDate;
        deadLine.plusDays(keepDay);
        Period p = deadLine.until(nowDate);
        return (p.getDays() > 0)||(p.getMonths() > 0)||(p.getYears()>0);
    }

    public abstract String toString();//抽象toString方法

    public abstract String getType();//抽象方法,返回饮料类型，toString时用
}

class Beer extends Drinks {  //啤酒类
    protected float alcoholDegree;//酒精度数

    public Beer(String name,double cost,LocalDate produceDate,float alcoholDegree) {
        super(name,cost,produceDate,30);
        this.alcoholDegree = alcoholDegree;
    }

    @Override
    public String toString() { //toString
        return("啤酒:"+name+"\n" +
                "成本:"+cost+"\n" +
                "生产日期:"+produceDate+"\n"+
                "保质期:"+keepDay+"天\n"+
                "酒精度数:"+alcoholDegree+"\n");
    }

    @Override
    public String getType() { //返回饮料类型(toString时用)
        return("啤酒");
    }
}

class Juice extends Drinks {  //果汁类
    public Juice(String name,double cost,LocalDate produceDate) {
        super(name,cost,produceDate,2);
    }

    @Override
    public String toString() { //toString
        return("果汁:"+name+"\n" +
                "成本:"+cost+"\n" +
                "生产日期:"+produceDate+"\n"+
                "保质期:"+keepDay+"天\n");
    }

    @Override
    public String getType() { //返回饮料类型(toString时用)
        return("果汁");
    }
}

class SetMeal { //套餐类
    protected String name;//套餐名
    protected double price;//套餐价
    protected String chickenName;//炸鸡名
    protected Drinks drink;//饮料(多态)

    public SetMeal(String name,double price,String chickenName,Drinks drink) {
        this.name = name;
        this.price = price;
        this.chickenName = chickenName;
        this.drink = drink;
    }

    @Override
    public String toString() {
        return("套餐名:"+name+"\n" +
                "套餐价:"+price+"\n" +
                "炸鸡名:"+chickenName+"\n"+
                "饮料名:"+drink.name+"("+drink.getType()+")\n");
    }
}

interface FriedChickenRestaurant { //炸鸡店接口
    void sellMeal(String mealName); //出售套餐
    void inputGoods(Beer b,int num); //批量进货
    void inputGoods(Juice j,int num); //批量进货
}

class IngredientSortOutException extends RuntimeException { //自定义异常类 果汁啤酒售完
    public IngredientSortOutException() {
        super();
    }

    public IngredientSortOutException(String message) {
        super(message);
    }
}

class OverdraftBalanceException extends RuntimeException { //自定义异常类 进货费用超出拥有余额
    public OverdraftBalanceException() {
        super();
    }

    public OverdraftBalanceException(String message) {
        super(message);
    }
}

class West2FriedChickenRestauran implements FriedChickenRestaurant {
    protected double balance;//餐厅余额

    protected LinkedList<Beer> beerList = new LinkedList<>();//啤酒链表
    protected LinkedList<Juice> juiceList = new LinkedList<>();//果汁链表
    /*出售啤酒和果汁需要频繁的消除操作，故选择linkedList*/

    protected static ArrayList<SetMeal> mealList = new ArrayList<>();//套餐链表
    static {
        LocalDate nowDate = LocalDate.now(); // 当前日期
        //啤酒
        Beer b1 = new Beer("黄泉酒",999.99,nowDate,250);
        //果汁
        Juice j1 = new Juice("美汁汁",2.5,nowDate);
        //套餐
        mealList.add(new SetMeal("阴间啤酒餐",1099.99,"送终鸡",b1));
        mealList.add(new SetMeal("美汁汁餐",11.10,"香香鸡",j1));
        mealList.add(new SetMeal("儿童套餐",22.10,"小炸鸡",j1));
    }
    /*套餐只需要遍历查找，选择内存较小的arraylist*/

    //构造函数
    public West2FriedChickenRestauran(double b) {
        this.balance = b;
    }

    public void use(Beer b) { //移除啤酒b
        boolean isFind = false;
        for (Iterator<Beer> it = beerList.iterator(); it.hasNext(); ) {
            if (it.next().name.equals(b.name)) { //找到第一个符合的并删除
                it.remove();
                isFind = true;
                break;
            }
        }
        if (!isFind) { //没找到，抛出异常
            throw new IngredientSortOutException("啤酒 "+b.name+"已售完！");
        }
    }

    public void use(Juice j) { //移除果汁j
        boolean isFind = false;
        for (Iterator<Juice> it = juiceList.iterator(); it.hasNext(); ) {
            if (it.next().name.equals(j.name)) { //找到第一个符合的并删除
                it.remove();
                isFind = true;
                break;
            }
        }
        if (!isFind) { //没找到，抛出异常
            throw new IngredientSortOutException("果汁 "+j.name+"已售完！");
        }
    }

    @Override
    public void sellMeal(String mealName) { //出售套餐
        boolean isSell = false;
        for (SetMeal sm : mealList) {
            if (mealName.equals(sm.name)) {
                try { //尝试卖套餐
                    if (sm.drink instanceof Beer) {
                        use((Beer)sm.drink);
                        isSell = true;
                    }
                    else if (sm.drink instanceof Juice){
                        use((Juice)sm.drink);
                        isSell = true;
                    }

                    balance+=sm.price;//卖套餐赚钱
                }
                catch (IngredientSortOutException e) {
                    System.out.println(e.getMessage()+"无法售出套餐！");
                }
            }
        }
        if (isSell)
            System.out.println("出售"+mealName+"成功");
    }

    @Override
    public void inputGoods(Beer b,int num) { //批量进货啤酒
        LocalDate nowDate = LocalDate.now(); // 当前日期
        if (balance<num*b.cost) {
            throw new OverdraftBalanceException("购买"+b.name+"*"+num+"的余额不够");
        }
        for (int i=0;i<num;i++) {
            beerList.add(b);
        }
        balance -= num*b.cost;
    }

    @Override
    public void inputGoods(Juice j,int num) { //批量进货果汁
        LocalDate nowDate = LocalDate.now(); // 当前日期
        if (balance<num*j.cost) {
            throw new OverdraftBalanceException("购买"+j.name+"*"+num+"的余额不够");
        }
        for (int i=0;i<num;i++) {
            //果汁
            juiceList.add(j);
        }
        balance -= num*j.cost;
    }
}