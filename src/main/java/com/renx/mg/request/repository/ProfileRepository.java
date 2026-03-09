package com.renx.mg.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renx.mg.request.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {


}
