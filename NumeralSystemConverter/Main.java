/* https://hyperskill.org/projects/41?goal=7
#About
We’re all quite used to our good old decimal system of numerals.
 But let’s not forget that there are countless other ways to count!
 Whether we convert numbers from one system to another just for fun
 or to store large data more efficiently, a converter would be helpful.
 In this project you will create a mathematical helper
 that will help you convert numbers from system M to system N.
 
 Some work with binaries.
*/
package converter;

public class Main {
    private static int parseDigit(char ch) {
        return ch <= '9' ? ch - '0' : 10 + ch - 'a';
    }

    private static char convertDigit(long digit) {
        return (char) (digit < 10 ? '0' + digit : 'a' + digit - 10);
    }

    private static long parseLong(int base, String num) {
        long res = 0;
        for (char ch : num.toCharArray()) {
            res = res * base + parseDigit(ch);
        }
        return res;
    }

    private static double parseFractional(int base, String num) {
        double res = 0;
        double invBase = 1.0 / base;
        for (int i = num.length() - 1; i >= 0; i--) {
            res = invBase * res + parseDigit(num.charAt(i));
        }
        return invBase * res;
    }

    private static boolean checkDigit(int base, char ch) {
        return '0' <= ch && ch <= Math.min('9', '0' + base - 1)
                || base > 10 && ch >= 'a' && ch < 'a' + base - 10;
    }

    private static double parse(int base, String num) {
        if (base == 1) {
            return num.chars().allMatch(d -> d == '1') ? num.length() : Double.NaN;
        }
        if (base < 1 || base > 36) return Double.NaN;
        String[] intFrac = num.split("\\.");
        if (intFrac.length > 2) return Double.NaN;
        if (!intFrac[0].chars().allMatch(ch -> checkDigit(base, (char) ch)))
            return Double.NaN;
        double res = parseLong(base, intFrac[0]);
        if (intFrac.length > 1) {
            if (!intFrac[0].chars().allMatch(ch -> checkDigit(base, (char) ch)))
                return Double.NaN;
            res += parseFractional(base, intFrac[1]);
        }
        return res;
    }

    private static String convert(double num, int base) {
        if (base < 1 || base > 36) return null;
        if (base == 1) return "1".repeat((int) num);
        StringBuilder res = new StringBuilder();
        long integer = (long) num;
        num -= integer;
        if (integer == 0) {
            res.append("0");
        } else {
            StringBuilder intPart = new StringBuilder();
            while (integer > 0) {
                intPart.append(convertDigit(integer % base));
                integer /= base;
            }
            res.append(intPart.reverse().toString());
        }
        if (num > 0) {
            res.append('.');
            for (int i = 0; i < 5; i++) {
                num *= base;
                int digit = (int) num;
                res.append(convertDigit(digit));
                num -= digit;
            }
        }
        return res.toString();
    }

    public static void main(String[] args) {
        var sc = new java.util.Scanner(System.in);

        String[] input = new String[3];
        int i = 0;
        while (sc.hasNext()) {
            input[i++] = sc.next();
            if (i > 3) {
                System.out.println("Error : too many arguments");
                return;
            }
        }

        if (i < 3) {
            System.out.println("Error : not enough arguments");
            return;
        }

        double base1 = parse(10, input[0]);
        if (Double.isNaN(base1) || base1 != (int) base1) {
            System.out.println("Error : can't parse input base : " + input[0]);
            return;
        }
        int b1 = (int) base1;

        String num = input[1];

        double base2 = parse(10, input[2]);
        if (Double.isNaN(base2) || base2 != (int) base2) {
            System.out.println("Error : can't parse output base : " + input[2]);
            return;
        }
        int b2 = (int) base2;

        double number = parse(b1, num);
        if (Double.isNaN(number)) {
            System.out.println("Error : can't parse number " + num + " in base " + b1);
            return;
        }

        String res = convert(number, b2);
        if (res == null) {
            System.out.println("Error : can't convert " + number + " to base " + b2);
            return;
        }

        System.out.println(res);
    }
}
