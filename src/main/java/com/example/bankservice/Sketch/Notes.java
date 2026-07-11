package com.example.bankservice.Sketch;

public class Notes {
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


}

//List<Integer> numbers  = numbers.stream().map(n -> n*2 ).collect(Collectors.toList());
// List<String> numbers  = numbers.stream().map(n -> n + " TL").collect(Collectors.toList());
// List<Integer> numbers = numbers.stream().filter(n-> n %2 == 0).collect(Collectors.toList());


// List<BankAccount> accounts = accounts.stream()
//        .filter(accounts -> accounts.getBalance() > 1000)
//        .map(accounts ->  accounts.getName())
//        .collect(Collectors.toList());

/*
String result =
        names.stream()
             .map(name -> name.toUpperCase())
             .collect(Collectors.joining("- "))
 */
