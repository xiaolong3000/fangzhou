package dao;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 访问数据库的功能类
 * @author 夏冬琦  于 2012-1-1
 *
 */
public class Dao {

    private static String DRIVER=//"oracle.jdbc.OracleDriver";
            "com.mysql.cj.jdbc.Driver";
           // "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static String URL=//"jdbc:oracle:thin:@localhost:1521:orcl";
            "jdbc:mysql://111.229.7.110:3306/mydb";
           // "jdbc:sqlserver://localhost:1433;databaseName=bgw";
    private static String USER="open";//"tsgl";//"root";
    private static String PASSWORD="open";//"tsgl";//"root";//数据库连接密码

    //当前线程内数据库连接
    private static ThreadLocal<Connection> threadLocalConnection=new ThreadLocal<Connection>();
    //当前线程是否开启事务
    private static ThreadLocal<Boolean> threadLocalTransaction=new ThreadLocal<Boolean>();

    static{

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("没有找到驱动程序");
        }
    }

    private boolean isTransaction(){
        if(threadLocalTransaction.get()==null){
            threadLocalTransaction.set(false);
        }
        return threadLocalTransaction.get();
    }
    private void setTransaction(boolean transaction){

        threadLocalTransaction.set(transaction);

    }

    //预编译语句对象集合缓存
    private Set<PreparedStatement> pstmts=new HashSet<PreparedStatement>();;
    //当前预编译语句对象
    private PreparedStatement pstmt;
    //结果集对象集合缓存
    private Set<ResultSet> rss=new HashSet<ResultSet>();


    private String array2String(Object... params){

        StringBuffer buffer=new StringBuffer("{");
        if(params!=null){
            for(int i=0;i<params.length;i++){
                if(i==0)buffer.append("["+params[i]+"]");
                else buffer.append(","+"["+params[i]+"]");
            }
        }
        buffer.append("}");

        return buffer.toString();

    }

    private void preWork(PreparedStatement pst,Object... params) throws SQLException{

        if(params!=null){
            for(int i=0;i<params.length;i++){
                if(params[i] instanceof java.util.Date){
                    long time=((java.util.Date)params[i]).getTime();
                    pst.setTimestamp(i+1, new java.sql.Timestamp(time));
                }else{
                    pst.setObject(i+1, params[i]);
                }
            }
        }
    }

    private void preWork(String sql,Object... params) throws SQLException{

        pstmt = getConnection().prepareStatement(sql);
        pstmts.add(pstmt);
        this.preWork(pstmt, params);

    }

    /**
     * 关闭资源
     */
    public void close(){

        if(isTransaction()){
            System.out.println("资源关闭过程中回滚未提交事务！");
            this.rollback();
        }

        for(ResultSet rs:rss){
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
        }
        rss.clear();
        for(PreparedStatement pstmt:pstmts){
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
        }
        pstmts.clear();
        try {
            if (threadLocalConnection.get()!= null)
                if (!threadLocalConnection.get().isClosed())
                    threadLocalConnection.get().close();
        } catch (Exception e) {}
        threadLocalConnection.set(null);
    }

    /**
     * 执行查询语句
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    public ResultSet query(String sql,Object... params) throws SQLException{
        if(!isTransaction()){
            this.close();
            throw new RuntimeException("未开启事务！");
        }
        System.out.println("查询sql:"+sql);
        System.out.println("sql参数:"+this.array2String(params));
        try {
            this.preWork(sql, params);
            ResultSet rs = pstmt.executeQuery();
            rss.add(rs);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            this.rollback();
            throw e;
        }catch (RuntimeException e) {
            e.printStackTrace();
            this.rollback();
            throw e;
        }

    }

    /**
     * 执行更新语句
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    public Dao update(String sql,Object... params) throws SQLException{
        if(!isTransaction()){
            this.close();
            throw new RuntimeException("未开启事务！");
        }
        System.out.println("更新sql:"+sql);
        System.out.println("sql参数:"+this.array2String(params));
        try {
            this.preWork(sql, params);
            pstmt.executeUpdate();
            return this;
        }catch (SQLException e) {
            e.printStackTrace();
            this.rollback();
            throw e;
        }catch (RuntimeException e) {
            e.printStackTrace();
            this.rollback();
            throw e;
        }

    }

    /**
     * 获取预编译sql对象
     * @param sql
     * @return
     * @throws SQLException
     */
    public PreSQL createPreSQL(String sql) throws SQLException {

        if(!isTransaction()){
            close();
            throw new RuntimeException("未开启事务！");
        }



        class PreSqlImpl implements PreSQL{
            private String sql;
            private PreparedStatement pst;

            private PreSqlImpl(String sql) throws SQLException{
                this.sql=sql;
                this.pst=getConnection().prepareStatement(sql);
                pstmts.add(pst);
            }


            public PreSQL update(Object... params) throws SQLException {
                System.out.println("更新sql:"+sql);
                System.out.println("sql参数:"+array2String(params));
                try {
                    preWork(pst, params);
                    pst.executeUpdate();
                    pst.clearParameters();
                    return this;
                }catch (SQLException e) {
                    e.printStackTrace();
                    rollback();
                    throw e;
                }catch (RuntimeException e) {
                    e.printStackTrace();
                    rollback();
                    throw e;
                }
            }


            public ResultSet query(Object... params) throws SQLException {
                System.out.println("查询sql:"+sql);
                System.out.println("sql参数:"+array2String(params));
                try {
                    preWork(pst, params);
                    ResultSet rs=pst.executeQuery();
                    rss.add(rs);
                    pst.clearParameters();
                    return rs;
                }catch (SQLException e) {
                    e.printStackTrace();
                    rollback();
                    throw e;
                }catch (RuntimeException e) {
                    e.printStackTrace();
                    rollback();
                    throw e;
                }

            }


        }


        try {
            return new PreSqlImpl(sql);
        }catch (SQLException e) {
            e.printStackTrace();
            rollback();
            throw e;
        }catch (RuntimeException e) {
            e.printStackTrace();
            rollback();
            throw e;
        }


    }

    private static Connection getConnection() throws SQLException{
        Connection conn=threadLocalConnection.get();
        if(conn==null){
            conn = DriverManager.getConnection(
                    URL,USER,PASSWORD);
            conn.setAutoCommit(false);
            threadLocalConnection.set(conn);
        }
        return conn;
    }


    /**
     * 开启事务
     * @return
     * @throws SQLException
     */
    public Dao beginTransaction() throws SQLException{
        if(isTransaction()){
            this.rollback();
            throw new RuntimeException("事务已开启，尚未结束，不允许重复开启事务！");
        }
        getConnection();
        this.setTransaction(true);
        System.out.println("---开始事务---");
        return this;
    }

    /**
     * 提交事务
     * @return
     */
    public Dao commitTransaction(){
        if(!isTransaction()){
            throw new RuntimeException("未开启事务！");
        }
        try {
            getConnection().commit();
            this.setTransaction(false);
            System.out.println("---事务提交---");
        } catch (SQLException e) {
            e.printStackTrace();
            this.rollback();
        }

        return this;
    }

    private void rollback(){
        try{
            if(!isTransaction()){
                throw new RuntimeException("未开启事务！");
            }
            try{
                getConnection().rollback();
                System.out.println("---事务回滚---");
            }catch(SQLException e){
                e.printStackTrace();
                System.out.println("事务回滚异常！原因：" +e.getMessage());
            }
        }finally{
            this.setTransaction(false);
        }
    }

    /**
     * 初始化
     * @param url
     * @param user
     * @param password
     */
    static void init(String url, String user, String password) {
        URL=url;
        USER=user;
        PASSWORD=password;
    }




}
