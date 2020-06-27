package com.cherokeelessons.maze.entity;

public class Avatar {
	public enum Gender {
		Achvya, Achuja
	}

	public enum Race {
		Jisdu, Saloli, Waya
	}

	public Race race = Race.Jisdu;
	public Gender gender = Gender.Achuja;
}
