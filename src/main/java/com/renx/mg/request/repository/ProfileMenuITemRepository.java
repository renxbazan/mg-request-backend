package com.renx.mg.request.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.renx.mg.request.model.ProfileMenuItem;

public interface ProfileMenuITemRepository extends JpaRepository<ProfileMenuItem, Long> {

	@Query("SELECT p FROM ProfileMenuItem p JOIN FETCH p.menuItem m WHERE p.profileId = :profileId ORDER BY m.position")
	List<ProfileMenuItem> findByProfileIdOrderByMenuItemPosition(@Param("profileId") Long profileId);
}
