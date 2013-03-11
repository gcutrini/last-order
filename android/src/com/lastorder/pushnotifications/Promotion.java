package com.lastorder.pushnotifications;

import java.io.Serializable;
import java.util.GregorianCalendar;


public class Promotion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -877304088280126719L;
	public long id = 0L;
	public String venue = "";
	public String name = "";
	public String description = "";
	public int discount = 0;
	public double price = 0;
	public GregorianCalendar expiration = new GregorianCalendar();
	public String address = "";
	public double lat = 0;
	public double lon = 0;
	public String url_image= "";
	
}
