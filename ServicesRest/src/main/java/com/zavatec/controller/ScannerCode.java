package com.zavatec.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zavatec.model.Product;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@RestController
public class ScannerCode {
	private List<Product> productList;

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Autowired
	private DataSource dataSource;

	public ScannerCode() throws IOException {
		FileReader file = new FileReader("image.txt");
		BufferedReader br = new BufferedReader(file);

		Product p = new Product();
		p.setCount(4);
		p.setPrice(100.0);
		p.setName("MotherBoard");
		p.setSerialCode("ABC123");
		p.setDescription("Computador IBM para recambio");
		String sCurrentLine;

		while ((sCurrentLine = br.readLine()) != null) {
			p.setImageB64(sCurrentLine);
		}

		this.productList = new ArrayList<>();
		this.productList.add(p);
	}

	@CrossOrigin
	@GetMapping(value = "/getProducts")
	public List<Product> getProduct() {
		List<Product> list = new ArrayList<Product>();

		try (Connection connection = dataSource.getConnection()) {
			Statement stmt = connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT product_name, product_description, product_count, product_serialcode, image64 FROM public.productos");
			Product p = new Product();
			while (rs.next()) {

				p.setName(rs.getString("name"));
				p.setDescription(rs.getString("description"));
				p.setSerialCode(rs.getString("serialCode"));
				p.setCount(rs.getInt("count"));
				p.setImageB64(rs.getString("image64"));
				list.add(p);
				p = new Product();

			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return list;
	}

	@CrossOrigin
	@PostMapping(value = "/setProduct")
	public String setProduct(@RequestBody Product product) {
		System.out.println(product);
		String query = "";
		try {
			this.productList.add(product);
			String res = "";
			try (Connection connection = dataSource.getConnection()) {
				Statement stmt = connection.createStatement();
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS productos ("
						+ "product_name VARCHAR(100), product_description VARCHAR(100), product_count INT, product_serialcode TEXT, image64 TEXT)");
				query = "INSERT INTO public.productos(" + 
						"	product_name, product_description, product_count, product_serialcode, image64) " + 
						"	VALUES "
						+ " ('" + product.getName() + "','" + product.getDescription() + "'," + product.getCount()
						+ ",'" + product.getSerialCode() + "','" + product.getImageB64() + "' )";
				stmt.executeUpdate(query);
				res = "OK";
			} catch (Exception e) {
				return res += "message " + e.getMessage() + product.toString() + " " + query;

			}
			return " SIN ERRORES: " + product.toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@CrossOrigin
	@GetMapping(value = "/getProduct/{id}")
	public Product getProductById(@PathVariable("id") int id) {

		return this.productList.get(id);
	}

	@RequestMapping("/db")
	String db(Map<String, Object> model) {
		String res = "";
		try (Connection connection = dataSource.getConnection()) {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS products ("
					+ "name text, description text, count int, serialCode text)");
			stmt.executeUpdate("INSERT INTO products VALUES ('abc123','desc',4,''basrqw'))");
			ResultSet rs = stmt.executeQuery("SELECT * FROM products");

			ArrayList<String> output = new ArrayList<String>();

			while (rs.next()) {
				res += "  Read from DB: " + rs.getString("name");
			}

			return res;
		} catch (Exception e) {
			res += "message " + e.getMessage();
			return res;
		}
	}

	@Bean
	public DataSource dataSource() throws SQLException {
		if (dbUrl == null || dbUrl.isEmpty()) {
			return new HikariDataSource();
		} else {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(dbUrl);
			return new HikariDataSource(config);
		}
	}

}
