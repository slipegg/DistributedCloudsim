package com.companyname.bank;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

public class ObjectToMap {
  public static void main(String[] args) {
    // 创建一个对象
    Person person = new Person("John", 30);

    // 将对象转换为 Map
    Map<String, Object> map = objectToMap(person);

    // 打印 Map
    System.out.println(map);
    System.out.println(map.get("name"));
  }

  public static Map<String, Object> objectToMap(Object obj) {
    // 创建一个 Map
    Map<String, Object> map = new HashMap<>();

    // 获取对象的类型
    Class clazz = obj.getClass();

    // 获取对象的所有属性
    Field[] fields = clazz.getDeclaredFields();

    // 遍历所有属性
    for (Field field : fields) {
      // 设置属性可访问
      field.setAccessible(true);

      // 获取属性名称
      String name = field.getName();

      // 获取属性值
      Object value = null;
      try {
        value = field.get(obj);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }

      // 将属性名称和属性值放入 Map 中
      map.put(name, value);
    }

    // 返回 Map
    return map;
  }

  // 定义一个 Person 类
  public static class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }
  }
}
