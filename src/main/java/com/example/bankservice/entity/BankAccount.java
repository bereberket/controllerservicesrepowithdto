package com.example.bankservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table

public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long id;
    @Column(name = "NAMELY", nullable = false)
    private String name;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false,unique = true)
    private String accountNumber;


    public boolean equals(Object o){
        if(this == o){ return true;}
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        BankAccount that =(BankAccount)o;
        return accountNumber.equals(that.accountNumber);

    }

    public int hashCode(){
        return accountNumber.hashCode();
    }
    public long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public double getBalance(){
        return balance;
    }
    public String getAccountNumber(){
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber){
        this.accountNumber = accountNumber;}

    public void setName(String name){
        this.name = name;
    }
    public void setBalance(double balance){
        this.balance = balance;
    }



}



//Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
//while (it.hasNext()) {
//    Map.Entry<String, Integer> entry = it.next();
//    if (entry.getValue() < 0) {
//        it.remove();  //
//    }
//



//public class BankAccount implements Comparable<BankAccount> {
//
//                                                                  ------>>>   treeset içindeki comparable implementasyonu
//    @Override
//    public int compareTo(BankAccount other) {                    ----------------->>>>>> compareTo() inceler.
//        // Hesap numarasına göre sırala
//        return this.accountNumber.compareTo(other.accountNumber);
//    }
//}



//// Bakiyeye göre sıralayan Comparator
//Comparator<BankAccount> balanceComparator = new Comparator<>() {                     ---------->>>> biz bi comperator veririz burada.Örnekte de balance'a göre sıralamış.
//    @Override
//    public int compare(BankAccount a1, BankAccount a2) {
//        return Double.compare(a1.getBalance(), a2.getBalance());
//    }
//};
//
//TreeSet<BankAccount> treeSet = new TreeSet<>(balanceComparator);

