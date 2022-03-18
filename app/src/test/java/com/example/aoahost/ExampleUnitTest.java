package com.example.aoahost;


import com.example.aoahost.producercustomer.BufferedArea;
import com.example.aoahost.producercustomer.Customer;
import com.example.aoahost.producercustomer.Producer;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        //assertEquals(4, 2 + 2);
        /*byte[] test = new byte[10];
        byte[] test1 = "1".getBytes();
        System.arraycopy(test1,0,test,0,1);
        System.out.println("length:"+test.length);
        System.out.println("actuallylength:"+new String(Communicator.ActualBytes(test)));*/
        byte[] test = new byte[24];
        for (int i=0;i<3;i++){
            test[i] = 0;
        }
        for (int i=3;i<8;i++){
            test[i] = 1;
        }
        for (int i=8;i<11;i++){
            test[i] = 0;
        }
        test[11]=1;
        for (int i=12;i<16;i++){
            test[i] = 2;
        }
        for (int i=16;i<19;i++){
            test[i] = 0;
        }
        test[19]=1;
        for (int i=20;i<24;i++){
            test[i] = 3;
        }

        BufferedArea buffer = new BufferedArea();
        Producer producer = new Producer(buffer);
        Customer customer = new Customer(buffer) {
            @Override
            public void onRender(byte[] payload) {
                System.out.println("消费者从仓库取出商品:" + Arrays.toString(payload));
            }
        };
        producer.setProduct(test,test.length);
        Thread thread1 = new Thread(producer);
        Thread thread2 = new Thread(customer);
        thread1.start();
        thread2.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        producer.setProduct(test,test.length);
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}