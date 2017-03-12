package domain.repository.quill

import fujitask.{ReadTransaction, ReadWriteTransaction, Transaction}
import io.getquill.{H2JdbcContext, SnakeCase}

abstract class QuillTransaction(val ctx: H2JdbcContext[SnakeCase]) extends Transaction

class QuillReadTransaction(ctx: H2JdbcContext[SnakeCase]) extends QuillTransaction(ctx) with ReadTransaction

class QuillReadWriteTransaction(ctx: H2JdbcContext[SnakeCase]) extends QuillTransaction(ctx) with ReadWriteTransaction