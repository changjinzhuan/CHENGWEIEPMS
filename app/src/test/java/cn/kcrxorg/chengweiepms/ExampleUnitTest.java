package cn.kcrxorg.chengweiepms;

import android.util.Log;

import com.rscja.utility.StringUtility;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.kcrxorg.chengweiepms.locktools.CRC;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

       // assertEquals(4, 2 + 2);
                                                     //001CCAF51FA496EB7F8C4DAF777186DFE3189407465C39C1BAD7
        byte[] datab=StringUtility.hexString2Bytes("001CCAF51FA496EB7F8C4DAF777186DFE3189407465C39C1BAD7");
        int[] datais=new int[datab.length];
        for(int i=0;i<datab.length;i++)
        {
            datais[i]=(int)datab[i];
        }
        //int[] testdatais={0x00,0x04};


        int calcCrc16 = CRC.calcCrc16(datab);
        final String hex  = CRC.crc16(datais);;
        System.out.println("hex="+hex);
        System.out.println("calcCrc16="+String.format("%04x", calcCrc16).toUpperCase());
    }
    @Test
    public void timeTest()
    {
        Date date = new Date(Long.parseLong("000001787d2bdf2d", 16));//时间戳计算
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");
        System.out.println("日期为:"+sdf1.format(date));
    }
}