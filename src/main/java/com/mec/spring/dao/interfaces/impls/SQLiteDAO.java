package com.mec.spring.dao.interfaces.impls;


import com.mec.spring.dao.interfaces.MP3Dao;
import com.mec.spring.dao.objects.Author;
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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public int insert(MP3 mp3) {
        System.out.println(TransactionSynchronizationManager.isActualTransactionActive());
        int authorId = insertAuthor(mp3);
        MapSqlParameterSource params;

        String sqlMp3 = "insert into mp3 (name, author_id) values (:mp3Name, :authorId)";
        params = new MapSqlParameterSource();
        params.addValue("mp3Name", mp3.getName());
        params.addValue("authorId", authorId);

        return jdbcTemplate.update(sqlMp3, params);

//        MapSqlParameterSource params = new MapSqlParameterSource();
//        params.addValue("name", mp3.getName());
//        params.addValue("author", mp3.getAuthor());
//
//        return insertMP3.execute(params);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int insertAuthor(MP3 mp3) {
        System.out.println(TransactionSynchronizationManager.isActualTransactionActive());
        String sqlAuthor = "insert into author (name) VALUES (:authorName)";

        Author author = mp3.getAuthor();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("authorName", author.getName());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sqlAuthor, params, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public int insertBatch(List<MP3> list) {
        String sql = "insert into mp3 (name, author) VALUES (:name, :author)";
        SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(list.toArray());
        int[] updateCounts = jdbcTemplate.batchUpdate(sql, batch);
        return updateCounts.length;
    }

    @Override
    public int insert(List<MP3> list) {
        for (MP3 mp3 : list) {
            insert(mp3);
        }
        return list.size();
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
        String sql = "select author_name, count(*) as count from mp3_view group by author_name";

        return jdbcTemplate.query(sql, new ResultSetExtractor<Map<String, Integer>>() {

            public Map<String, Integer> extractData(ResultSet rs) throws SQLException {
                Map<String, Integer> map = new TreeMap<>();
                while (rs.next()) {
                    String author = rs.getString("author_name");
                    int count = rs.getInt("count");
                    map.put(author, count);
                }
                return map;
            }
        });

    }

    @Override
    public MP3 getMP3ByID(int id) {
        String sql = "select * from mp3_view where mp3_id=:mp3_id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("mp3_id", id);

        try {
            return jdbcTemplate.queryForObject(sql, params, new MP3RowMapper());
        } catch (DataAccessException e) {
            System.out.println("Элемент не найден");
            return null;
        }
    }

    @Override
    public List<MP3> getMP3ListByName(String name) {
        String sql = "select * from mp3_view where upper(mp3_name) like :mp3_name";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("mp3_name", "%" + name.toUpperCase() + "%");

        return jdbcTemplate.query(sql, params, new MP3RowMapper());
    }

    @Override
    public List<MP3> getMP3ListByAuthor(String author) {
        String sql = "select * from mp3_view where upper(author_name) like :author_name";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("author_name", "%" + author.toUpperCase() + "%");

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

            Author author = new Author();
            author.setId(rs.getInt("author_id"));
            author.setName(rs.getString("author_name"));

            MP3 mp3 = new MP3();
            mp3.setId(rs.getInt("mp3_id"));
            mp3.setName(rs.getString("mp3_name"));
            mp3.setAuthor(author);
            return mp3;
        }

    }
}

