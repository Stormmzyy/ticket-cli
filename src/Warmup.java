public class Warmup {

    // Exercise 1: FizzBuzz from 1 to 20
    public static void fizzBuzz() {
        for (int i = 1; i <= 20; i++) {
            if (i % 15 == 0) {
                System.out.println("FizzBuzz");
            } else if (i % 3 == 0) {
                System.out.println("Fizz");
            } else if (i % 5 == 0) {
                System.out.println("Buzz");
            } else {
                System.out.println(i);
            }
        }
    }

    // Exercise 2: palindrome check (ignores case, spaces and punctuation)
    static boolean isPalindrome(String s) {
        if (s == null) {
            return false;
        }

        String cleaned = s.toLowerCase().replaceAll("[^a-z0-9]", "");
        int left = 0;
        int right = cleaned.length() - 1;

        while (left < right) {
            if (cleaned.charAt(left) != cleaned.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    // Exercise 3a: largest value in an array
    static int largest(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }

        int max = numbers[0];
        for (int n : numbers) {
            if (n > max) {
                max = n;
            }
        }
        return max;
    }

    // Exercise 3b: average of an array
    static double average(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }

        long sum = 0;
        for (int n : numbers) {
            sum += n;
        }
        return (double) sum / numbers.length;
    }

    public static void main(String[] args) {
        System.out.println("=== FizzBuzz ===");
        fizzBuzz();

        System.out.println("\n=== Palindromes ===");
        String[] words = {"racecar", "level", "hello", "Madam", "A man, a plan, a canal: Panama", "java"};
        for (String word : words) {
            System.out.println("\"" + word + "\" -> " + isPalindrome(word));
        }

        System.out.println("\n=== Arrays ===");
        int[] numbers = {4, 17, -3, 25, 8, 12};
        System.out.println("Largest: " + largest(numbers));
        System.out.printf("Average: %.2f%n", average(numbers));
    }
}