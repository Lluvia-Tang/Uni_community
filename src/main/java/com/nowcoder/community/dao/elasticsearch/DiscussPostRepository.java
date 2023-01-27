package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository //只需要继承 spring自动实现
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> { //传入类和主键类型
}
