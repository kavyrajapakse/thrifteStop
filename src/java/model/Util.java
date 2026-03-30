package model;

public class Util {
    
//    public static String generateCode(){
//       int r = (int) (Math.random()*1000000);
//       return String.format("%06d", r);
//    }
//    
//    public static boolean isEmailValied(String email){
//        return email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
//    }
    
    public static boolean isPasswordValied(String password){
        return password.matches("^.(?=.{8,})(?=..[0-9])(?=.[a-z])(?=.[A-Z])(?=.[@#$%^&+=]).$");
    }
    
    public static boolean isCodeValied(String code){
        return code.matches("^\\d{4,5}$");
    }
    
    public static boolean isInteger(String value){
        return value.matches("^?\\d+$");
    }
    
     public static boolean isDouble(String text){
        return text.matches("^\\d+(\\.\\d{2})?$");
    }
}