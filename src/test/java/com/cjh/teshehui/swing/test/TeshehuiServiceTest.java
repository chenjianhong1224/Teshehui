package com.cjh.teshehui.swing.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.cjh.teshehui.swing.service.TeshehuiService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TeshehuiServiceTest {

	@Autowired
	TeshehuiService teshehuiService;
	
	@Test
	public void testGetStockInfo(){
	}
}
