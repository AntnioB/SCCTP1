package scc.srv;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import scc.auction.AuctionDAO;
import scc.auction.CosmosDBAuctionLayer;
import scc.cache.RedisCache;
import scc.utils.Status;

import java.time.ZonedDateTime;

import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class TimerFunction {
    @FunctionName("periodic-compute")
    public void cosmosFunction(@TimerTrigger(name = "periodicSetTime", schedule = "30 */5 * * * *") String timerInfo,
            ExecutionContext context) {
        CosmosDBAuctionLayer auctionLayer = CosmosDBAuctionLayer.getInstance();
        Iterator<AuctionDAO> openAuctions = auctionLayer.getOpenAuctions().iterator();
        ZonedDateTime curreTime = ZonedDateTime.now();

        while (openAuctions.hasNext()) {
            AuctionDAO auction = openAuctions.next();
            if (curreTime.isAfter(auction.getEndTime())) {
                auction.setStatus(Status.CLOSED);
                auctionLayer.updateAuction(auction);

                RedisCache.deleteAuction(auction.getId());
            }
        }

    }
}