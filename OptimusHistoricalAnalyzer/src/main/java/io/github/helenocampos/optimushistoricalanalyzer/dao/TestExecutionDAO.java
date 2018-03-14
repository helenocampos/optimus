/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.optimushistoricalanalyzer.dao;

import io.github.helenocampos.optimushistoricalanalyzer.domain.TestExecution;
import io.github.helenocampos.optimushistoricalanalyzer.domain.TestGranularity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author helenocampos
 */
public class TestExecutionDAO
{

    private String dbURL;

    public TestExecutionDAO(String dbURL)
    {
        this.dbURL = "jdbc:sqlite:" + dbURL;
        createTablesStructure();
    }

    private void createTablesStructure()
    {
        String sql = "CREATE TABLE IF NOT EXISTS `project` (\n"
                + "  `id_project` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "  `project_name` VARCHAR(100) NULL);\n";
        executeSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS `test` (\n"
                + "  `id_test` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "  `test_name` VARCHAR(255) NULL,\n"
                + "  `granularity` INT NULL,\n"
                + "  `project_id` INT NOT NULL,\n"
                + "  FOREIGN KEY(project_id) REFERENCES project(id_project));";
        executeSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS `testExecution` (\n"
                + "  `id_execution` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "  `executionTime` DOUBLE NULL,\n"
                + "  `result` BOOLEAN NULL,\n"
                + "  `timeStamp` BIGINT NULL,\n"
                + "  `project_id` INT NOT NULL,\n"
                + "  `test_id` INT NOT NULL,\n"
                + "  FOREIGN KEY(project_id) REFERENCES project(id_project),\n"
                + " FOREIGN KEY(test_id) REFERENCES test(id_test));\n";
        executeSQL(sql);

    }

    public void insertExecution(TestExecution test)
    {
        int projectId = getProjectId(test.getProjectName());
        if (projectId == Integer.MIN_VALUE)
        {
            projectId = insertProject(test.getProjectName());
        }

        int testId = getTestId(test.getTestName(), test.getGranularity().getId(), projectId);
        if (testId == Integer.MIN_VALUE)
        {
            testId = insertTest(test.getTestName(), test.getGranularity().getId(), projectId);
        }

        String sql = "INSERT INTO testExecution (executionTime, result, timeStamp, project_id, test_id) VALUES(?,?,?,?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setDouble(1, test.getExecutionTime());
            pstmt.setBoolean(2, test.isResult());
            pstmt.setLong(3, test.getTimeStamp());
            pstmt.setInt(4, projectId);
            pstmt.setInt(5, testId);
            pstmt.executeUpdate();
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private int getProjectId(String projectName)
    {
        String sql = "select id_project from project where project_name = '" + projectName + "'";
        int projectId = Integer.MIN_VALUE;
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql))
        {

            while (rs.next())
            {
                projectId = rs.getInt("id_project");
            }
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return projectId;
    }

    private int getTestId(String testName, int testGranularity, int projectId)
    {
        String sql = "select id_test from test where test_name = '" + testName + "' and granularity = " + testGranularity
                + " and project_id=" + projectId;
        int testId = Integer.MIN_VALUE;
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql))
        {

            while (rs.next())
            {
                testId = rs.getInt("id_test");
            }
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return testId;
    }

    private int getTestId(String testName, int projectId)
    {
        String sql = "select id_test from test where test_name = '" + testName + "' and project_id=" + projectId;
        int testId = Integer.MIN_VALUE;
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql))
        {

            while (rs.next())
            {
                testId = rs.getInt("id_test");
            }
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return testId;
    }

    private int insertTest(String testName, int granularity, int projectId)
    {
        String sql = "INSERT INTO test (test_name, granularity, project_id) VALUES(?,?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, testName);
            pstmt.setInt(2, granularity);
            pstmt.setInt(3, projectId);
            pstmt.executeUpdate();
            return getTestId(testName, granularity, projectId);

        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return Integer.MIN_VALUE;
    }

    private int insertProject(String projectName)
    {
        String sql = "INSERT INTO project (project_name) VALUES(?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, projectName);
            pstmt.executeUpdate();
            return getProjectId(projectName);

        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return Integer.MIN_VALUE;
    }

    private Connection connect()
    {
        try
        {
            return DriverManager.getConnection(this.dbURL);
        } catch (SQLException ex)
        {
            Logger.getLogger(TestExecutionDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<TestExecution> selectAllEntries()
    {
        String sql = "select * from testExecution INNER JOIN project on testExecution.project_id=project.id_project INNER JOIN test on testExecution.test_id = test.id_test";
        List<TestExecution> entries = new LinkedList<TestExecution>();
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql))
        {

            while (rs.next())
            {
                TestExecution entry = instantiateTestExecution(rs);
                if (entry != null)
                {
                    entries.add(entry);
                }

            }
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return entries;
    }

    public List<TestExecution> selectAllEntriesByTestName(String testName, String projectName)
    {
        List<TestExecution> entries = new LinkedList<TestExecution>();
        int projectId = getProjectId(projectName);
        if (projectId != Integer.MIN_VALUE)
        {
            int testId = getTestId(testName, projectId);
            if (testId != Integer.MIN_VALUE)
            {
                String sql = "select * from testExecution INNER JOIN project on testExecution.project_id=project.id_project INNER JOIN test on testExecution.test_id = test.id_test WHERE test.id_test=? and project.id_project=?";

                try (Connection conn = this.connect();
                        PreparedStatement pstmt = conn.prepareStatement(sql))

                {
                    pstmt.setInt(1, testId);
                    pstmt.setInt(2, projectId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next())
                    {
                        TestExecution entry = instantiateTestExecution(rs);
                        if (entry != null)
                        {
                            entries.add(entry);
                        }
                    }
                } catch (SQLException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }

        return entries;
    }

    public int getTestFailureAmount(String testName, String projectName)
    {
        int failureAmount = 0;
        int projectId = getProjectId(projectName);
        if (projectId != Integer.MIN_VALUE)
        {
            int testId = getTestId(testName, projectId);
            if (testId != Integer.MIN_VALUE)
            {

                String sql = "SELECT count(id_execution) as executions FROM testExecution WHERE test_id = ? and result = 0";

                try (Connection conn = this.connect();
                        PreparedStatement pstmt = conn.prepareStatement(sql))

                {
                    pstmt.setInt(1, testId);
                    ResultSet rs = pstmt.executeQuery();

                    failureAmount = rs.getInt("executions");

                } catch (SQLException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        return failureAmount;
    }

    public int getTestExecutionAmount(String testName, String projectName)
    {
        int executionAmount = 0;
        int projectId = getProjectId(projectName);
        if (projectId != Integer.MIN_VALUE)
        {
            int testId = getTestId(testName, projectId);
            if (testId != Integer.MIN_VALUE)
            {
                String sql = "SELECT count(id_execution) as executions FROM testExecution WHERE test_id = ? and project_id = ?";

                try (Connection conn = this.connect();
                        PreparedStatement pstmt = conn.prepareStatement(sql))
                {
                    pstmt.setInt(1, testId);
                    pstmt.setInt(2, projectId);
                    ResultSet rs = pstmt.executeQuery();
                    executionAmount = rs.getInt("executions");

                } catch (SQLException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        return executionAmount;
    }

    private void executeSQL(String sql)
    {
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement())
        {
            stmt.execute(sql);
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private TestExecution instantiateTestExecution(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                return new TestExecution(rs.getInt("id_execution"), rs.getString("test_name"),
                        TestGranularity.getGranularityById(rs.getInt("granularity")), rs.getDouble("executionTime"),
                        rs.getBoolean("result"), rs.getLong("timeStamp"), rs.getString("project_name"));
            } catch (SQLException ex)
            {
                Logger.getLogger(TestExecutionDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

}
