-- 替换 ES 全文搜索：在 commodity 表的 name、description 字段建 FULLTEXT 索引
-- MySQL 8.x 的 InnoDB 原生支持 FULLTEXT，配合 ngram parser 支持中文分词
ALTER TABLE commodity ADD FULLTEXT INDEX ft_commodity_search(name, description) WITH PARSER ngram;
