package com.example.parkhonolulu;

import android.provider.ContactsContract;

public class Manager {

    private String name;
    private String surname;
    private String email;
    private String password;

    private ContactsContract.CommonDataKinds.Phone phone;

    public Manager(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContactsContract.CommonDataKinds.Phone getPhone() {
        return phone;
    }

    public void setPhone(ContactsContract.CommonDataKinds.Phone phone) {
        this.phone = phone;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
