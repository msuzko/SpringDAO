package com.mec.spring.main;

import com.mec.spring.dao.interfaces.impls.SQLiteDAO;
import com.mec.spring.dao.objects.Author;
import com.mec.spring.dao.objects.MP3;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class Start {

    public static void main(String[] args) {
        Author author = new Author();
        author.setName("Singer #5");

        MP3 mp3 = new MP3("Song #5",author);

        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        SQLiteDAO sqLiteDAO = context.getBean("sqliteDAO",SQLiteDAO.class);

       // sqLiteDAO.insert(mp3);
//        MP3 mp32 = sqLiteDAO.getMP3ByID(2000);

        System.out.println(sqLiteDAO.insert(mp3));
        System.out.println(sqLiteDAO.getStat());

//        List<MP3> list = new ArrayList<>();
//        list.add(new MP3("Song #10", "Author #4"));
//        list.add(new MP3("Song #11", "Author #4"));
//        list.add(new MP3("Song #12", "Author #4"));
//       System.out.println(sqLiteDAO.insert(list));

    }
}
