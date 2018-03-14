package io.github.helenocampos.optimushistoricalanalyzer.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author helenocampos
 */
public class DB
{
    private String url;
    
    public DB(String url){
        this.url = "jdbc:sqlite:"+url;
    }
    
}
