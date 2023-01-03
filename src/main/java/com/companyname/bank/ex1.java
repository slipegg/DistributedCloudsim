package com.companyname.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.io.*;

public class ex1 {

    public static void main(String[] args) {

        List<Person> list = new ArrayList<Person>();

        Person p1 = new Person("d", 55);
        Person p2 = new Person("c", 18);
        Person p3 = new Person("a", 37);

        list.add(p1);
        list.add(p2);
        list.add(p3);
        sortPerson(list);
    }

    public static void sortPerson(List<Person> list) {
        Collections.sort(list);// 编译通过；
        for (Person person : list) {
            System.out.println(person);
        }
    }
}

class Person implements Comparable<Person> {
    public String name;
    public int age;

    public Person(String name, int age) {
        super();
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

    @Override
    public String toString() {
        return "Person [name=" + name + ", age=" + age + "]";
    }

    @Override
    public int compareTo(Person o) {
        return this.getName().compareTo(o.getName());
    }
}