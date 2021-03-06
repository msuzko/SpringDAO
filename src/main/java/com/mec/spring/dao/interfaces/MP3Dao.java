package com.mec.spring.dao.interfaces;

import com.mec.spring.dao.objects.MP3;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface MP3Dao {

    int insert(MP3 mp3);

    @Transactional(propagation = Propagation.REQUIRED)
    int insertAuthor(MP3 mp3);

    int insert(List<MP3> list);

    void delete(MP3 mp3);

    void delete(int id);

    Map<String, Integer> getStat();

    MP3 getMP3ByID(int id);

    List<MP3> getMP3ListByName(String name);

    List<MP3> getMP3ListByAuthor(String author);

    int getMP3Count();

}
