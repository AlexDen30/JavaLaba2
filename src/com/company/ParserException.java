package com.company;

public class ParserException extends Exception{

    private String errorString;  //  Описание ошибки

    public ParserException(String errorStr) {
        super();
        this.errorString = errorStr;
    }

    public String toString(){
        return this.errorString;
    }
}