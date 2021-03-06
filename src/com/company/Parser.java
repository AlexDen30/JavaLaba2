package com.company;

import java.util.Scanner;

public class Parser {


    /** Текущая лексема */
    private String currentLexem;
    /** Тип текущей лексемы */
    private int lexemType;
    /** Строка-выражение */
    private String valueString;
    /** Текущий индекс в строке */
    private int currentPositionInString;

    //лексемы
    /** Индикатор ошибки */
    final int NONE = 0;
    /** Разделитель +-.^=)( */
    final int SEPARATOR = 1;
    /** Переменная */
    final int VARIABLE = 2;
    /** Число */
    final int NUMBER = 3;


    /** Синтаксическая ошибка: н-р 1 2 + 10  */
    final int SYNTAXERROR = 0;
    /** Количество открывающих и
     * закрывающих скобок не совпало */
    final int BRACKETSNUMBER = 1;
    /** Нет выражения */
    final int MISSINGEXPRESSION = 2;
    /** Деление на ноль */
    final int DIVBYZERO = 3;
    /** Конец строки*/
    final String EOF = "\0";

    /**
     * Выбор ошибки
     *
     * @param numOfError - номер ошибки
     * @throws ParserException
     */
    private void handleError(int numOfError) throws ParserException{

        String[] err  = {
                "Syntax error",
                "Equality of ( and ) brackets violated",
                "No Expression",
                "Division by zero"
        };
        throw new ParserException(err[numOfError]);
    }

    /**
     * <b>Получить следующую лексему</b>
     * <br>
     *<ul>
     *     <li>1. Проверяем на окончание выражения.</li>
     *     <li>2. Затем на пробелы, пропускаем их.</li>
     *     <li>3. Далее ещё раз проверяем на окончание выражения.</li>
     *     <li>4. Не является ли разделителем?</li>
     *     <li> 5. Не является ли переменной?</li>
     *     <li>6. Не является ли числом?</li>
     *     <li>7. Варианты исчерпаны - конец строки </li>
     *</ul>
     */
    private void getNextLexem(){

        lexemType = NONE;
        currentLexem = "";

        if(currentPositionInString == valueString.length()){
            currentLexem = EOF;
            return;
        }

        while(currentPositionInString < valueString.length()
                && Character.isWhitespace(valueString.charAt(currentPositionInString)))
            ++currentPositionInString;

        if(currentPositionInString == valueString.length()){
            currentLexem = EOF;
            return;
        }

        if(isSeparator(valueString.charAt(currentPositionInString))) {
            currentLexem += valueString.charAt(currentPositionInString);
            currentPositionInString++;
            lexemType = SEPARATOR;
        } else if(Character.isLetter(valueString.charAt(currentPositionInString))) {
            while(!isSeparator(valueString.charAt(currentPositionInString))){
                currentLexem += valueString.charAt(currentPositionInString);
                currentPositionInString++;

                if(currentPositionInString >= valueString.length())
                    break;
            }

            lexemType = VARIABLE;
        } else if (Character.isDigit(valueString.charAt(currentPositionInString))) {
            while(!isSeparator(valueString.charAt(currentPositionInString))){
                currentLexem += valueString.charAt(currentPositionInString);
                currentPositionInString++;
                if(currentPositionInString >= valueString.length())
                    break;
            }
            lexemType = NUMBER;
        }

        else {
            currentLexem = EOF;
            return;
        }
    }

    /**
     * Проверка - является ли символ разделителем
     *
     * @param symbol - символ
     * @return результат проверки
     */
    private boolean isSeparator(char symbol) {
        if((" +-/*%^=()".indexOf(symbol)) != -1)
            return true;
        return false;
    }

    /**
     * Анализатор выражения
     *<br>
     * Инициализируем поля, проверяем строку
     * на пустоту, затем начинаем вычисление по дереву
     * операций
     *<br>
     * <b>Приоритеты:</b>
     * <ul>
     * <li>0. Получить значение числа</li>
     * <li>1. Выражение в скобках</li>
     * <li>2. Знак числа</li>
     * <li>3. Возведение в степень</li>
     * <li>4. Деление или умножение</li>
     * <li>5. Сложение вычитание</li>
     *</ul>
     * <b>В процессе вычисления мы последователно переходим по
     * этим приоритетам, если нет более высоких по приоритету
     * операций, то осуществляем текущую. Для каждого приоритета
     * есть метод, в котором, если нужно, вызывается метод
     * более высокого приоритета
     * <b/>
     * @param stringToCalculate - выражение записанное в строке
     * @return вычисленный результат выражения
     * @throws ParserException
     */

    public double evaluate(String stringToCalculate) throws ParserException{

        double result;

        valueString = stringToCalculate;

        //если есть переменные заменяем их
        for(char i = 'a'; i<='z'; i++)
        {
            String substr = "" + i;

            if (valueString.contains(substr)) {
                Scanner in = new Scanner(System.in);
                System.out.println("Enter " + i + ":");

                double varValue = in.nextDouble();
                valueString = valueString.replace(substr, Double.toString(varValue));
                System.out.println(valueString);
            }

        }

        currentPositionInString = 0;
        getNextLexem();

        if(currentLexem.equals(EOF))
            handleError(MISSINGEXPRESSION);



        result = expression5();

        if(!currentLexem.equals(EOF))
            handleError(SYNTAXERROR);

        return result;
    }


    /**
     * Складываем или вычитаем два выражения,
     * если нет более приоритетных операций, иначе переходим к ним
     * (5 - низший приоритет)
     *
     * @return
     * @throws ParserException
     */
    private double expression5() throws ParserException{

        char operation;
        double result;
        double partialResult;
        result = expression4();
        while((operation = currentLexem.charAt(0)) == '+' ||
                operation == '-'){
            getNextLexem();
            partialResult = expression4();
            switch(operation){
                case '-':
                    result -= partialResult;
                    break;
                case '+':
                    result += partialResult;
                    break;
            }
        }
        return result;
    }


    /**
     * Умножение или деление или остаток от деления
     * (4 приоритет)
     * @return
     * @throws ParserException
     */
    private double expression4() throws ParserException{

        char operation;
        double result;
        double partialResult;

        result = expression3();
        while((operation = currentLexem.charAt(0)) == '*' ||
                operation == '/' | operation == '%'){
            getNextLexem();
            partialResult = expression3();
            switch(operation){
                case '*':
                    result *= partialResult;
                    break;
                case '/':
                    if(partialResult == 0.0)
                        handleError(DIVBYZERO);
                    result /= partialResult;
                    break;
                case '%':
                    if(partialResult == 0.0)
                        handleError(DIVBYZERO);
                    result %= partialResult;
                    break;
            }
        }
        return result;
    }

    /**
     * Возведение в степень
     * (приоритет 3)
     *
     * @return
     * @throws ParserException
     */
    private double expression3() throws ParserException{

        double result;
        double partialResult;
        double ex;
        int t;
        result = expression2();
        if(currentLexem.equals("^")){
            getNextLexem();
            partialResult = expression3();
            ex = result;
            if(partialResult == 0.0){
                result = 1.0;
            }else
                for(t = (int)partialResult - 1; t >  0; t--)
                    result *= ex;
        }
        return result;
    }

    /**
     * Определение знака числа
     * (2 приоритет)
     *
     * @return
     * @throws ParserException
     */
    private double expression2() throws ParserException{
        double result;

        String operation;
        operation = " ";

        if((lexemType == SEPARATOR) && currentLexem.equals("+") ||
                currentLexem.equals("-")){
            operation = currentLexem;
            getNextLexem();
        }
        result = expression1();
        if(operation.equals("-"))
            result =  -result;
        return result;
    }

    /**
     * Обработка выражения в скобках.
     * (1 приоритет)
     *
     * @return
     * @throws ParserException
     */
    private double expression1() throws ParserException{
        double result;

        if(currentLexem.equals("(")){
            getNextLexem();
            result = expression5();
            if(!currentLexem.equals(")"))
                handleError(BRACKETSNUMBER);
            getNextLexem();
        }
        else
            result = expression0();
        return result;
    }

    /**
     * Переводим символьное значение в число.
     * (приоритет 0)
     *
     * @return
     * @throws ParserException
     */
    private double expression0()   throws ParserException{

        double result = 0.0;
        switch(lexemType){
            case NUMBER:
                try{
                    result = Double.parseDouble(currentLexem);
                }
                catch(NumberFormatException exc){
                    handleError(SYNTAXERROR);
                }
                getNextLexem();

                break;
            default:
                handleError(SYNTAXERROR);
                break;
        }
        return result;
    }


}