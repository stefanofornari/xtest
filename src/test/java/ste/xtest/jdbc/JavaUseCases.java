package ste.xtest.jdbc;

import java.util.List;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Date;
import org.assertj.core.util.Lists;


import static ste.xtest.jdbc.RowList.Column;
import static ste.xtest.jdbc.RowLists.booleanList;
import static ste.xtest.jdbc.RowLists.intList;
import ste.xtest.jdbc.StatementHandler.Parameter;

/**
 * Use cases for testing.
 *
 * @author Cedric Chantepie
 */
public final class JavaUseCases {
    // Configure in anyway JDBC with following url,
    // declaring handler registered with 'my-handler-id' will be used.
    protected static final String jdbcUrl =
        "jdbc:xtest:anything-you-want?handler=my-handler-id";

    /**
     * Use case #1 - Quick start
     */
    public static Connection useCase1() throws SQLException {
        // Prepare handler
        final StatementHandler handler = new CompositeHandler().
            withQueryDetection("^SELECT ", // regex test from beginning
                               "EXEC that_proc"). // second detection regex
            withUpdateHandler(new CompositeHandler.UpdateHandler() {
                    // Handle execution of update statement (not query)
                    public UpdateResult apply(String sql,
                                              List<Parameter> parameter) {

                        if (sql.startsWith("DELETE ")) {
                            // Process deletion ...

                            return /* deleted = */new UpdateResult(2);
                        }

                        // ... Process ...

                        return /* count = */UpdateResult.One;
                    }
                }).
            withQueryHandler(new CompositeHandler.QueryHandler () {
                    public QueryResult apply(String sql,
                                             List<Parameter> params) {

                        if (sql.startsWith("SELECT ")) {
                            return new QueryResult.Default(
                               new RowList(String.class)
                            );
                        }

                        // ... EXEC that_proc (see previous withQueryDetection)

                        // Prepare list of 2 rows
                        // with 3 columns of types String, Float, Date
                        return new QueryResult.Default(
                                new RowList(String.class, Float.class, Date.class)
                                    .withLabel(1, "String")
                                    .withLabel(3, "Date")
                                    .append(List.of("str", 1.2f, new Date(1l)))
                                    .append(Lists.newArrayList("val", 2.34f, null)) // List.of() can not contain nulls...
                        );
                    }
                });

        // Register prepared handler with expected ID 'my-handler-id'
        ste.xtest.jdbc.Driver.register("my-handler-id", handler);

        // ... then connection is managed through |handler|
        return (ste.xtest.jdbc.Connection)DriverManager.getConnection(jdbcUrl);
    } // end of useCase1

    /**
     * Use case #2 - Column definitions
     */
    public static Connection useCase2() throws SQLException {
        final StatementHandler handler = new CompositeHandler().
            withQueryDetection("^SELECT ").
            withQueryHandler(new CompositeHandler.QueryHandler() {
                    public QueryResult apply(String sql,
                                             List<Parameter> params) {

                        // Prepare list of 2 rows
                        // with 3 columns of types String, Float, Date
                        return new QueryResult.Default(
                                new RowList(Column(String.class, "str"),
                                           Column(Float.class, "f"),
                                           Column(Date.class, "date"))
                            .append(List.of("text", 2.3f, new Date(3l)))
                            .append(List.of("label", 4.56f, new Date(4l)))
                        );
                    }
                });

        // Register prepared handler with expected ID 'my-handler-id'
        ste.xtest.jdbc.Driver.register("my-handler-id", handler);

        // ... then connection is managed through |handler|
        return (ste.xtest.jdbc.Connection)DriverManager.getConnection(jdbcUrl);
    } // end of useCase2

    /**
     * Use case #3 - Warnings
     */
    public static Connection useCase3() throws SQLException {
        final StatementHandler handler = new CompositeHandler().
            withQueryDetection("^SELECT ").
            withQueryDetection("^EXEC ").
            withQueryHandler(new CompositeHandler.QueryHandler() {
                    public QueryResult apply(String sql,
                                             List<Parameter> params) {

                        if (sql.startsWith("EXEC ")) {
                            return QueryResult.
                                Empty.withWarning(new SQLWarning("Warn EXEC"));

                        } // end of if

                        return QueryResult.Empty;
                    }
                }).
            withUpdateHandler(new CompositeHandler.UpdateHandler() {
                    // Handle execution of update statement (not query)
                    public UpdateResult apply(String sql,
                                              List<Parameter> parameter) {

                        if (sql.startsWith("DELETE ")) {
                            return UpdateResult.
                                Nothing.withWarning(new SQLWarning("Warn DELETE"));

                        } // end of if

                        return UpdateResult.Nothing;
                    }
                });

        // Register prepared handler with expected ID 'my-handler-id'
        ste.xtest.jdbc.Driver.register("my-handler-id", handler);

        // ... then connection is managed through |handler|
        return (ste.xtest.jdbc.Connection)DriverManager.getConnection(jdbcUrl);
    } // end of useCase3

    /**
     * Use case #4 - Row list convinience constructor.
     */
    public static Connection useCase4() throws SQLException {
        return ste.xtest.jdbc.Driver.
            connection(new CompositeHandler().
                       withQueryDetection("^SELECT ").
                       withQueryHandler(new CompositeHandler.QueryHandler() {
                               public QueryResult apply(String sql,
                                                        List<Parameter> ps) {
                                    return new QueryResult.Default(
                                        booleanList().append(List.of(true))
                                    );
                               }
                           }));


    } // end of useCase4

    /**
     * Use case #5 - Generated keys
     */
    public static Connection useCase5() throws SQLException {
        return ste.xtest.jdbc.Driver.
            connection(new CompositeHandler().
                       withUpdateHandler(new CompositeHandler.UpdateHandler() {
                               public UpdateResult apply(String sql,
                                                         List<Parameter> ps) {

                                   // One update with generated keys
                                   return UpdateResult.One.
                                       withGeneratedKeys(intList().append(List.of(100)));
                               }
                           }));


    } // end of useCase5
} // end of class JavaUseCases