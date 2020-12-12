import java.util.concurrent.*;
import java.util.Scanner;

public class MultTask {
        public static void main(String[] args) throws Exception {

            Scanner scanner = new Scanner(System.in);
            int x = scanner.nextInt();

            int n = 1000000000;
            // fork/join:
            ForkJoinTask<Long> task = new SumTask(x, 1, n);
            Long result = ForkJoinPool.commonPool().invoke(task);
            System.out.println( "" + result );
        }

    }

    class SumTask extends RecursiveTask<Long> {
        static final int THRESHOLD = 1000;
        int x;
        int start;
        int end;

        private static boolean contain(long num, int x) {
            return String.valueOf(num).contains(String.valueOf(x));
        }

        SumTask(int x,int start, int end) {
            this.x = x;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                // 如果任务足够小,直接计算:
                long sum = 0;
                for (int i = start; i < end; i++) {
                    if (contain(i, x)) sum += i;
                }
                return sum;
            }
            // 任务太大,一分为二:
            int middle = (end + start) / 2;
            SumTask subtask1 = new SumTask(x, start, middle);
            SumTask subtask2 = new SumTask(x, middle, end);
            invokeAll(subtask1, subtask2);
            Long subresult1 = subtask1.join();
            Long subresult2 = subtask2.join();
            Long result = subresult1 + subresult2;
            return result;
        }
    }

