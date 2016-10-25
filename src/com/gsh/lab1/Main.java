package com.gsh.lab1;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lab.Main;

public class Main {

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String expressionCache = "";
    while (true) {
      System.out.print(">");
      String input = scanner.nextLine();
      // 预处理
      input = input.trim();
      input = input.replaceAll("\\t", "");
      input = input.replaceAll("\\s+", " ");
      // 表达式
      if (input.matches("^([0-9]+|[a-zA-Z]+)((\\+|\\*)([0-9]+|[a-zA-Z]+))+$")) {
        expressionCache = input;
        System.out.println(expressionCache);

        // 化简
      } else if (input.matches("^\\!simplify(\\s[a-zA-Z]+=[0-9]+)*$")) {
        System.out.println(Main.simplify(expressionCache, input));

        // 求导
      } else if (input.matches("^\\!d/d[a-zA-Z]+$")) {
        System.out.println(Main.derivative(expressionCache, input));

        // 退出
      } else if (input.matches("!exit")) {
        break;

        // 错误输入
      } else {
        System.out.println("Error: Unrecognized input");
        expressionCache = "";
      }
    }
    scanner.close();
  }

  public static String simplify(String expression, String command) {
    // 变量替换 匹配command中的键值对
    Pattern pattern = Pattern.compile("[a-zA-Z]+=[0-9]+");
    Matcher matcher = pattern.matcher(command);
    while (matcher.find()) {
      String result = matcher.group();
      int equalIndex = result.indexOf("=");
      if (!expression.contains(result.substring(0, equalIndex))) {
        return "Error: undifined variable";
      }
      expression = expression.replaceAll(result.substring(0, equalIndex),
          result.substring(equalIndex + 1));
    }
    // 乘法化简 匹配所有乘法子式
    pattern = Pattern.compile("([0-9]+|[a-zA-Z]+)(\\*([0-9]+|[a-zA-Z]+))+");
    matcher = pattern.matcher(expression);
    while (matcher.find()) {
      String result = matcher.group();
      expression = expression.replace(result, Main.computeMulti(result));
    }
    // 加法化简
    expression = Main.computeAdd(expression);

    return expression;
  }

  public static String computeMulti(String expression) {
    Pattern pattern1 = Pattern.compile("[0-9]+");
    Pattern pattern2 = Pattern.compile("[a-zA-Z]+");
    Matcher matcher1 = pattern1.matcher(expression);
    Matcher matcher2 = pattern2.matcher(expression);
    int numResult = 1;
    List<String> vars = new ArrayList<>();
    while (matcher1.find()) {
      numResult = numResult * Integer.valueOf(matcher1.group());
    }
    while (matcher2.find()) {
      vars.add(matcher2.group());
    }
    StringBuffer result = new StringBuffer("" + numResult);
    for (String var : vars) {
      result.append("*" + var);
    }
    return result.toString();
  }

  public static String computeAdd(String expression) {
    Pattern pattern = Pattern.compile("(|\\+)[a-zA-Z0-9|\\*]+");
    Matcher matcher = pattern.matcher(expression);
    List<String> subExpressions = new ArrayList<>();
    int numResult = 0;
    while (matcher.find()) {
      String subExpression = matcher.group();
      if (subExpression.startsWith("+")) {
        subExpression = subExpression.substring(1);
      }
      if (!subExpression.contains("*")) {
        numResult = numResult + Integer.valueOf(subExpression);
      } else {
        subExpressions.add(subExpression);
      }
    }
    StringBuffer stringBuffer = new StringBuffer();
    if (numResult != 0) {
      stringBuffer.append(numResult);
      for (String subExpression : subExpressions) {
        stringBuffer.append("+" + subExpression);
      }
    } else {
      stringBuffer.append(subExpressions.remove(0));
      for (String subExpression : subExpressions) {
        stringBuffer.append("+" + subExpression);
      }
    }
    return stringBuffer.toString();
  }

  public static String derivative(String expression, String command) {
    String var = command.substring(4);
    if (!expression.contains(var)) {
      return "Error: undifined variable";
    }
    String[] subExpressions = expression.split("\\+");
    String result = "";
    for (String subExpression : subExpressions) {
      if (subExpression.contains(var)) {
        int index = subExpression.indexOf(var);
        int startIndex = index - 1;
        int endIndex = index + var.length();
        int count = 0;
        Pattern pattern = Pattern.compile(var);
        Matcher matcher = pattern.matcher(subExpression);
        while (matcher.find()) {
          matcher.group();
          count++;
        }
        if (startIndex < 0) {
          startIndex = 0;
        }
        if (endIndex > subExpression.length()) {
          endIndex = subExpression.length();
        }
        String temp = subExpression.substring(0, startIndex) + subExpression.substring(endIndex);
        if (temp.startsWith("*") || temp.equals("")) {
          temp = count + temp;
        } else {
          temp = count + "*" + temp;
        }
        result = result + "+" + temp;
      }
    }
    result = Main.simplify(result, "!simplify");
    return result;
  }
}
