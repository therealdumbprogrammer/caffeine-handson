package com.thecodealchemist.main;

import com.github.benmanes.caffeine.cache.*;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@SpringBootApplication
public class CaffeineHandsonApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaffeineHandsonApplication.class, args);
	}

	@RestController
	class MovieController {

		//private Cache<String, String> movieCache;

		private LoadingCache<String, String> movieCache;

		@Autowired
		DataDao dao;

		@PostConstruct
		public void initCache() {
			movieCache = Caffeine.newBuilder()
					.maximumSize(2)
					//.expireAfterAccess(Duration.ofSeconds(5))
//					.refreshAfterWrite(Duration.ofSeconds(10))
					.removalListener(new RemovalListener<String, String>() {
						@Override
						public void onRemoval(@Nullable String key, @Nullable String value, RemovalCause removalCause) {
							System.out.println("Key = " + key +", value = " + value +" was removed. Reason = " + removalCause.toString());
						}
					})
					.evictionListener(new RemovalListener<String, String>() {
						@Override
						public void onRemoval(@Nullable String key, @Nullable String value, RemovalCause removalCause) {
							System.out.println("Key = " + key +", value = " + value +" was evicted. Reason = " + removalCause.toString());
						}
					})
					.build(dao::movieByDirector);
		}

		// [chad, John, Chistopher]
		@GetMapping("movie/{director}")
		public String movieByDirector(@PathVariable("director") String director) {
			return movieCache.get(director);
		}

		@GetMapping("invalidate/{key}")
		public String invalidate(@PathVariable String key) {
			movieCache.invalidate(key);
			return "Key = " + key +" successfully removed";
		}

	}

	@Repository
	class DataDao {
		private static final Map<String, String> DATA = Map.of(
				"John","Die Hard","George","Mad Max: Fury Road",
				"Chad","John Wick",
				"James","Terminator 2: Judgment Day",
				"The Wachowskis","The Matrix","Richard","Lethal Weapon",
				"Christopher","Mission: Impossible - Fallout",
				"Quentin","Kill Bill: Volume 1","Ridley","Gladiator"
		);

		Map<String, String> getAll() {
			System.out.println("DAO -> Calling getAll");
			return DATA;
		}

		String movieByDirector(String director) {
			System.out.println("DAO -> Calling movieByDirector -> " + director);
			return DATA.get(director);
		}
	}

}
