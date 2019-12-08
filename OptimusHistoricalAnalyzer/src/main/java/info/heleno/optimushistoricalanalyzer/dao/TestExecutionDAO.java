/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.heleno.optimushistoricalanalyzer.dao;

import info.heleno.optimushistoricalanalyzer.domain.TestCaseExecution;
import info.heleno.optimushistoricalanalyzer.domain.TestSetExecution;
import info.heleno.testing.TestGranularity;
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
        sql = "CREATE TABLE IF NOT EXISTS `testSetExecution` (id INTEGER PRIMARY KEY, timeStamp BIGINT, "
                + "project_id INTEGER REFERENCES project (id_project))";
        executeSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS `testCaseExecution` (\n"
                + "  `id_execution` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
                + "  `executionTime` DOUBLE NULL,\n"
                + "  `result` BOOLEAN NULL,\n"
                + "  `test_id` INT NOT NULL,\n"
                + "  `testSetExecutionId` INTEGER REFERENCES testSetExecution (id), \n"
                + " FOREIGN KEY (test_id) REFERENCES test (id_test)); \n";

        executeSQL(sql);

    }

    public void insertExecution(TestCaseExecution test, int testSetId, int projectId)
    {
        test.setTestSetId(testSetId);
        int testId = getTestId(test.getTestName(), test.getGranularity().getId(), projectId);
        if (testId == Integer.MIN_VALUE)
        {
            testId = insertTest(test.getTestName(), test.getGranularity().getId(), projectId);
        }

        String sql = "INSERT INTO testCaseExecution (executionTime, result, testSetExecutionId, test_id) VALUES(?,?,?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setDouble(1, test.getExecutionTime());
            pstmt.setBoolean(2, test.isResult());
            pstmt.setInt(3, test.getTestSetId());
            pstmt.setInt(4, testId);
            pstmt.executeUpdate();
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void insertTestSetExecution(TestSetExecution testSet)
    {
        int projectId = getProjectId(testSet.getProjectName());
        if (projectId == Integer.MIN_VALUE)
        {
            projectId = insertProject(testSet.getProjectName());
        }

        String sql = "INSERT INTO testSetExecution (timeStamp, project_id) VALUES(?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setLong(1, testSet.getTimeStamp());
            pstmt.setInt(2, projectId);
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next())
            {
                int testSetId = generatedKeys.getInt(1);
                for (TestCaseExecution test : testSet.getExecutedTests())
                {
                    insertExecution(test, testSetId, projectId);
                }
            } else
            {
                throw new SQLException("Creating testSet failed, no ID obtained.");
            }
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

    public List<TestCaseExecution> selectAllEntries()
    {
        String sql = "select * from testCaseExecution INNER JOIN testSetExecution on testCaseExecution.testSetExecutionId=testSetExecution.id INNER JOIN project on testSetExecution.project_id=project.id_project INNER JOIN test on testCaseExecution.test_id = test.id_test";
        List<TestCaseExecution> entries = new LinkedList<>();
        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql))
        {

            while (rs.next())
            {
                TestCaseExecution entry = instantiateTestExecution(rs);
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

    public List<TestCaseExecution> getTestCaseExecutions(int testSetExecutionId, int testId)
    {
        String sql = "select * from testCaseExecution INNER JOIN test on testCaseExecution.test_id=test.id_test where testSetExecutionId=? and testCaseExecution.test_id=?";
        List<TestCaseExecution> entries = new LinkedList<>();
        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql))

        {
            pstmt.setInt(1, testSetExecutionId);
            pstmt.setInt(2, testId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next())
            {
                TestCaseExecution entry = instantiateTestExecution(rs);
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

    public List<TestCaseExecution> selectAllEntriesByTestName(String testName, String projectName)
    {
        List<TestCaseExecution> entries = new LinkedList<TestCaseExecution>();
        int projectId = getProjectId(projectName);
        if (projectId != Integer.MIN_VALUE)
        {
            int testId = getTestId(testName, projectId);
            if (testId != Integer.MIN_VALUE)
            {
                String sql = "select * from testCaseExecution INNER JOIN testSetExecution on testCaseExecution.testSetExecutionId=testSetExecution.id "
                        + "INNER JOIN project on testSetExecution.project_id=project.id_project "
                        + "INNER JOIN test on testCaseExecution.test_id = test.id_test WHERE test.test_name=? and project.project_name=?";

                try (Connection conn = this.connect();
                        PreparedStatement pstmt = conn.prepareStatement(sql))

                {
                    pstmt.setInt(1, testId);
                    pstmt.setInt(2, projectId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next())
                    {
                        TestCaseExecution entry = instantiateTestExecution(rs);
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

                String sql = "SELECT count(id_execution) as executions FROM testCaseExecution WHERE test_id = ? and result = 0";

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

    public List<TestSetExecution> getTestSetExecutions(String testName, String projectName)
    {
        List<TestSetExecution> testSetExecutions = new LinkedList<>();
        int projectId = getProjectId(projectName);
        if (projectId != Integer.MIN_VALUE)
        {
            int testId = getTestId(testName, projectId);
            if (testId != Integer.MIN_VALUE)
            {
                String sql = "SELECT * from testSetExecution INNER JOIN testCaseExecution on testSetExecution.id=testCaseExecution.testSetExecutionId INNER JOIN project on testSetExecution.project_id=project.id_project where project_id =? and testCaseExecution.test_id=? ORDER BY id";
                try (Connection conn = this.connect();
                        PreparedStatement pstmt = conn.prepareStatement(sql))
                {
                    pstmt.setInt(1, projectId);
                    pstmt.setInt(2, testId);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next())
                    {

                        TestSetExecution entry = instantiateTestSetExecution(rs);
                        if (entry != null)
                        {
                            List<TestCaseExecution> testCases = getTestCaseExecutions(entry.getId(),testId);
                            for (TestCaseExecution testCase : testCases)
                            {
                                entry.addExecutedTest(testCase);
                            }
                            testSetExecutions.add(entry);
                        }
                    }

                } catch (SQLException e)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
        return testSetExecutions;
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
                String sql = "SELECT count(id_execution) as executions FROM testCaseExecution WHERE test_id = ?";

                try (Connection conn = this.connect();
                        PreparedStatement pstmt = conn.prepareStatement(sql))
                {
                    pstmt.setInt(1, testId);
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

    private TestCaseExecution instantiateTestExecution(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                return new TestCaseExecution(rs.getInt("id_execution"), rs.getString("test_name"),
                        TestGranularity.getGranularityById(rs.getInt("granularity")), rs.getDouble("executionTime"),
                        rs.getBoolean("result"), rs.getInt("testSetExecutionId"));
            } catch (SQLException ex)
            {
                Logger.getLogger(TestExecutionDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    private TestSetExecution instantiateTestSetExecution(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                return new TestSetExecution(rs.getInt("id"), rs.getLong("timeStamp"),
                        rs.getString("project_name"));
            } catch (SQLException ex)
            {
                Logger.getLogger(TestExecutionDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

}
