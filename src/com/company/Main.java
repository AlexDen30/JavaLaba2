package com.company;


import java.util.Scanner;

public class Main {


    public static void main(String[] args) throws ParserException {
        Scanner in = new Scanner(System.in);
        Parser myParser = new Parser();


        try
        {
            System.out.println("Введите выражение для вычисления\n-> ");
            String str = in.nextLine();
            if(str.equals(""))
                return;
            double result = myParser.evaluate(str);

            System.out.println(result);

        }
        catch(ParserException e)
        {
            System.out.println(e);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }

    }

}

