package com.mec.spring.main;

import com.mec.spring.dao.interfaces.impls.SQLiteDAO;
import com.mec.spring.dao.objects.MP3;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Start {

    public static void main(String[] args) {
        MP3 mp3 = new MP3();
        mp3.setName("Song #2");
        mp3.setAuthor("Author #1");

        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        SQLiteDAO sqLiteDAO = context.getBean("sqliteDAO",SQLiteDAO.class);

       // sqLiteDAO.insert(mp3);
        //MP3 mp32 = sqLiteDAO.getMP3ByID(2);
        System.out.println(sqLiteDAO.getStat());
        System.out.println(sqLiteDAO.insert(mp3));

    }
}
