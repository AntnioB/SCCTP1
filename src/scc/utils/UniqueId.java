package scc.utils;
import java.util.UUID;

public class UniqueId {
    
    public static String randomUUID(String id){
        return id + "-" + UUID.randomUUID().toString().replace("-","");
    }

    public static String bidId(String id, int num){
        return "bid-" + num + "-" + id;
    }

    public static String questionId(){
        return "question-" + UUID.randomUUID().toString().replace("-","");
    }
}
