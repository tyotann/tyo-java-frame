package com.ihidea.core.support.dataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;

public class DynamicDataSourceTransactionManager extends DataSourceTransactionManager {

    /**
     * 本方法是如果开启了事务,才执行.
     * @param transaction
     * @param definition
     */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // 只要开启了事务, 就使用写库作为数据源
        DynamicDataSourceManager.setRoute(DynamicDataSourceEnum.WRITE.name().toLowerCase());
        super.doBegin(transaction, definition);
    }

    /**
     * 清理本地线程的数据源
     * @param transaction
     */
    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        super.doCleanupAfterCompletion(transaction);
        DynamicDataSourceManager.clean();
    }
}
