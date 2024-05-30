package com.petpal.petpalservice.repository;

import com.petpal.petpalservice.model.entity.Followers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import  java.util.List;

@Repository
public interface FollowersRepository extends JpaRepository<Followers, Integer> {
    Followers findByFollowerIdAndFollowedId(int followerId, int followedId);
    List<Followers> findByFollowedId(int followedId);
}
