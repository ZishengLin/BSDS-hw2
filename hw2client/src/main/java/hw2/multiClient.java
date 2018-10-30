package hw2;

import org.glassfish.jersey.client.ClientConfig;

import org.glassfish.jersey.jackson.JacksonFeature;


import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

class multiClient {

    static ConcurrentHashMap<Long, Integer> map = new ConcurrentHashMap<>();
    static Vector<Long> vector = new Vector<>();
    static Long totalStart = System.currentTimeMillis();
    static class Run extends Thread {
        int num;
        int startTime;
        int endTime;
        int day;
        int total;
        String address;
        int successfulRequest = 0;
        CountDownLatch latch;
        public Run(CountDownLatch latch,int num, int day, int startTime, int endTime, int total, String address) {
            this.latch = latch;
            this.num = num;
            this.startTime = startTime;
            this.endTime = endTime;
            this.day = day;
            this.total = total;
            this.address = address;
        }

        public void run() {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(JacksonFeature.class);
            Client client = ClientBuilder.newClient(clientConfig);
            Random r = new Random();


            for (int i = 0; i < num; i++) {
                int id1 = 1+r.nextInt(total);
                int id2 = 1+r.nextInt(total);
                while (id2 == id1) {
                    id2 = 1+r.nextInt(total);
                }
                int id3 = 1+r.nextInt(total);
                while (id3 == id2 || id3 == id1) {
                    id3 = 1+r.nextInt(total);
                }
                int stepCount1 = r.nextInt(5001);
                int stepCount2 = r.nextInt(5001);
                int stepCount3 = r.nextInt(5001);
                int timeInterval1 = startTime + r.nextInt(endTime - startTime + 1);
                int timeInterval2 = startTime + r.nextInt(endTime - startTime + 1);
                int timeInterval3 = startTime + r.nextInt(endTime - startTime + 1);

                long start=System.currentTimeMillis();
                try {
                    postData(id1, day, timeInterval1, stepCount1, client, address);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                try {
                    postData(id2, day, timeInterval2, stepCount2, client, address);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                getCurrent(id1, client, address);
                getSingle(id2, day, client, address);
                try {
                    postData(id3, day, timeInterval3, stepCount3, client, address);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                long end=System.currentTimeMillis();

                // record latency
                vector.add(end - start);
            }
            latch.countDown();
            System.out.println("end client");
            client.close();
        }

        @GET
        @Path("/current/{id}")
        @Produces(MediaType.APPLICATION_JSON)
        public void getCurrent(@PathParam("id") int id, Client client, String address) {

            WebTarget webTarget = client.target(address + "/current/" + id);

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();
            //int step = response.readEntity(Integer.class);
            if (response.getStatus() == 200) {
                //System.out.println(response.getStatus());
                successfulRequest++;
                long end=System.currentTimeMillis();
                map.put((end - totalStart)/1000, map.getOrDefault((end - totalStart)/1000, 0) + 1);
            }
        }

        @GET
        @Path("/single/{id}/{day}")
        @Produces(MediaType.APPLICATION_JSON)
        public void getSingle(@PathParam("id") int id, @PathParam("day") int day, Client client, String address) {

            WebTarget webTarget = client.target(address + "/single/" + id + "/" + day);

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();

            if (response.getStatus() == 200) {
                successfulRequest++;
                long end=System.currentTimeMillis();
                map.put((end - totalStart)/1000, map.getOrDefault((end - totalStart)/1000, 0) + 1);
            }
        }

        @POST
        @Path("/{userID}/{day}/{timeInterval}/{stepCount}/")
        @Consumes(MediaType.APPLICATION_XML)
        @Produces(MediaType.APPLICATION_XML)
        public void postData(int id, int day, int timeInterval, int stepCount, Client client, String address) throws URISyntaxException {

            WebTarget webTarget = client.target(address + "/" + id +
                    "/" + day + "/" + timeInterval + "/" + stepCount);

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.post(Entity.entity("\"sendInformation\"", MediaType.APPLICATION_JSON));

            if (response.getStatus() == 200) {
                successfulRequest++;
                long end=System.currentTimeMillis();
                map.put((end - totalStart)/1000, map.getOrDefault((end - totalStart)/1000, 0) + 1);
            }

        }
    }

    public static ResultType phase(double percent, int maxThread, int startTime, int endTime, int num, int day, int total, String address) {
        ArrayList<Run> ls = new ArrayList<>();

        ExecutorService threadPool =  Executors.newFixedThreadPool((int)(maxThread*percent));
        //BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<Runnable>(100);
        //ThreadPoolExecutor threadPool = new ThreadPoolExecutor(100, 100, 100, TimeUnit.MILLISECONDS, bqueue);
        String name = "";
        if (percent == 0.1) {
            name = "Warm up";
        } else if (percent == 0.5) {
            name = "Loading";
        } else if (percent == 1) {
            name = "Peaking";
        } else {
            name = "Cool down";
        }
        CountDownLatch latch = new CountDownLatch((int)(maxThread*percent));
        long time1 = System.currentTimeMillis();
        System.out.println(name + " phase "+ (int)(maxThread*percent) +" threads");
        for (int i = 0; i < maxThread * percent; i++) {
            Run t = new Run(latch,num, day, startTime, endTime, total, address);
            threadPool.execute(t);
            ls.add(t);
        }
//
//        try {
//            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        try {
            //stop current thread
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long time2=System.currentTimeMillis();
        int success = 0;
        for (Run t: ls) {

            success += t.successfulRequest;
        }
        threadPool.shutdown();

        System.out.println(name +" phase complete: Time "+ (time2-time1)/1000 +" s");
        return new ResultType(success, time2 - time1);
    }

    public static void main(String[] args) throws URISyntaxException {
        args = new String[]{"32", "url", "1", "100000", "100"};
        int maxThread = Integer.valueOf(args[0]);
        String address = args[1];
        int day = Integer.valueOf(args[2]);
        int total = Integer.valueOf(args[3]);
        int num = Integer.valueOf(args[4]);

        long success = 0;
        long wall = 0;

        System.out.println("Process start now");
        System.out.println("Client starting timing: " + new Date().toString());
        System.out.println("=========================================================================================");
        ResultType res1 = phase(0.1, maxThread, 0, 2, num*3, day, total, address);
        success += res1.success;
        wall += res1.time;

        ResultType res2 = phase(0.5, maxThread, 3, 7, num*5, day, total, address);
        System.out.println("can you see me");
        success += res2.success;
        wall += res2.time;


        ResultType res3 = phase(1, maxThread, 8, 18, num*11, day, total, address);
        success += res3.success;
        wall += res3.time;

        ResultType res4 = phase(0.25, maxThread, 19, 23, num*5, day, total, address);
        success += res4.success;
        wall += res4.time;

        WriteFile writer = new WriteFile(map, "next.csv");
        writer.write();

        System.out.println("================================================================");
        System.out.println("Total number of requests sent: "+ (int)(maxThread*num*(0.3+2.5+11+1.25))*5);
        System.out.println("Total number of Successful responses: "+ success);
        System.out.println("Test Wall Time: "+wall/1000+" seconds");
        System.out.println("Overall throughput across all phases: " + ((maxThread*num*(0.3+2.5+11+1.25))*5) / wall);


    }
}
class ResultType {
    long success;
    long time;
    public ResultType(long first, long time) {
        this.success = first;
        this.time = time;
    }
}
