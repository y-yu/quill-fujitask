package domain.service.exception


class UserNotFoundException(val message: String = null, val cause: Throwable = null)
  extends Exception(message, cause)
