package com.Tan.FightWithMorra.bean;

import java.util.ArrayList;
import java.util.List;
import com.Tan.FightWithMorra.bean.Point;

public class Player {
	private int id;
	private String name;
	private List<Tank> tanklist;
	private List<Plane> planelist;
	
	public Player(int id,String name){
		this.id = id;
		this.name = name;
		tanklist = new ArrayList<Tank>();
		planelist = new ArrayList<Plane>();
	}
	
	public void addTank(Point point){
		Tank tank = new Tank(point);
		tanklist.add(tank);
		
	}
	
	public void addPlane(Point point){
		Plane plane = new Plane(point);
		planelist.add(plane);
	}
}
