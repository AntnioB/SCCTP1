package scc.utils;
import java.util.UUID;

public class UniqueId {
    
    public static String randomUUID(String id){
        return id + "-" + uuid();
    }

    public static String auctionId(){
        return "auction-" + uuid();
    }

    public static String bidId(){
        return "bid-" + uuid();
    }

    public static String questionId(){
        return "question-" + uuid();
    }

    private static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
