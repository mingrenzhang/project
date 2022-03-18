package com.gavinandre.usbaccessory;

import com.gavinandre.usbaccessory.mediacodec.Sender;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        try{
            Sender.WriteMethod3(("123"+"\r\n").getBytes());
            Sender.WriteMethod3(("456"+"\r\n").getBytes());
            Sender.WriteMethod3(("789"+"\r\n").getBytes());
            Sender.WriteMethod3(("撒范德萨"+"\r\n").getBytes());
        }catch(Exception e){
            System.out.println(e);
        }
        //assertEquals(4, 2 + 2);
    }
}