package com.Tan.FightWithMorra.common.test;

import com.Tan.FightWithMorra.common.util.ConnectionFactory;

public class ConnTest {
	public static void main(String[] args) {
		try {
			System.out.println(ConnectionFactory.getConn());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
