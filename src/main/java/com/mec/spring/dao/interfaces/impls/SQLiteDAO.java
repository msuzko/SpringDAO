package com.mec.spring.dao.interfaces.impls;


import com.mec.spring.dao.interfaces.MP3Dao;
import com.mec.spring.dao.objects.MP3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component("sqliteDAO")
public class SQLiteDAO implements MP3Dao {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertMP3;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.insertMP3 = new SimpleJdbcInsert(dataSource).withTableName("mp3").usingColumns("name", "author");
    }

    @Override
    public int insert(MP3 mp3) {
//        String sql = "insert into mp3 (name, author) VALUES (:name, :author)";
//
//        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", mp3.getName());
        params.addValue("author", mp3.getAuthor());

        return insertMP3.execute(params);
//        jdbcTemplate.update(sql, params, keyHolder);
//
//        return keyHolder.getKey().intValue();
    }

//    public void insert(List<MP3> mp3List) {
//        for (MP3 mp3 : mp3List) {
//            insert(mp3);
//        }
//    }

    @Override
    public int insert(List<MP3> list) {
        String sql = "insert into mp3 (name, author) VALUES (:name, :author)";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(list.toArray());
        int[] updateCounts = jdbcTemplate.batchUpdate(sql, batch);
        return updateCounts.length;
    }

    @Override
    public void delete(int id) {
        String sql = "delete from mp3 where id=:id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        jdbcTemplate.update(sql, params);
    }

    @Override
    public void delete(MP3 mp3) {
        delete(mp3.getId());
    }

    @Override
    public Map<String, Integer> getStat() {
        String sql = "select author, count(*) as count from mp3 group by author";

        return jdbcTemplate.query(sql, new ResultSetExtractor<Map<String, Integer>>() {

            public Map<String, Integer> extractData(ResultSet rs) throws SQLException {
                Map<String, Integer> map = new TreeMap<>();
                while (rs.next()) {
                    String author = rs.getString("author");
                    int count = rs.getInt("count");
                    map.put(author, count);
                }
                return map;
            }
        });

    }

    @Override
    public MP3 getMP3ByID(int id) {
        String sql = "select * from mp3 where id=:id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        try {
            return jdbcTemplate.queryForObject(sql, params, new MP3RowMapper());
        } catch (DataAccessException e) {
            System.out.println("Элемент не найден");
            return null;
        }
    }

    @Override
    public List<MP3> getMP3ListByName(String name) {
        String sql = "select * from mp3 where upper(name) like :name";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", "%" + name.toUpperCase() + "%");

        return jdbcTemplate.query(sql, params, new MP3RowMapper());
    }

    @Override
    public List<MP3> getMP3ListByAuthor(String author) {
        String sql = "select * from mp3 where upper(author) like :author";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("author", "%" + author.toUpperCase() + "%");

        return jdbcTemplate.query(sql, params, new MP3RowMapper());
    }

    @Override
    public int getMP3Count() {
        String sql = "select count(*) from mp3";
        Integer result = jdbcTemplate.getJdbcOperations().queryForObject(sql, Integer.class);
        if (result != null)
            return result;
        return 0;
    }

    private static final class MP3RowMapper implements RowMapper<MP3> {

        @Override
        public MP3 mapRow(ResultSet rs, int rowNum) throws SQLException {
            MP3 mp3 = new MP3();
            mp3.setId(rs.getInt("id"));
            mp3.setName(rs.getString("name"));
            mp3.setAuthor(rs.getString("author"));
            return mp3;
        }

    }
}
