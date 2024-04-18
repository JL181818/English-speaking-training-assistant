package com.clankalliance.backbeta.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AntiInjection {

    public static boolean containsSqlInjection(String target){
        Pattern pattern = Pattern.compile(
                "\\b(and|exec|insert|select|drop|grant|alter|delete|update|count|chr|mid|master|truncate|char|declare|or)\\b|(\\*|;|\\+|'|%)");
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

}
