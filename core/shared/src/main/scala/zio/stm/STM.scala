/*
 * Copyright 2017-2020 John A. De Goes and the ZIO Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.stm

import scala.util.Try

import zio.{ Fiber, IO }

object STM {

  /**
   * Submerges the error case of an `Either` into the `STM`. The inverse
   * operation of `STM.either`.
   */
  def absolve[R, E, A](e: STM[E, Either[E, A]]): STM[E, A] =
    ZSTM.absolve(e)

  /**
   * Atomically performs a batch of operations in a single transaction.
   */
  def atomically[E, A](stm: STM[E, A]): IO[E, A] =
    ZSTM.atomically(stm)

  /**
   * Checks the condition, and if it's true, returns unit, otherwise, retries.
   */
  def check(p: => Boolean): STM[Nothing, Unit] = ZSTM.check(p)

  /**
   * Collects all the transactional effects in a list, returning a single
   * transactional effect that produces a list of values.
   */
  def collectAll[E, A](i: Iterable[STM[E, A]]): STM[E, List[A]] =
    ZSTM.collectAll(i)

  /**
   * Kills the fiber running the effect.
   */
  def die(t: => Throwable): STM[Nothing, Nothing] =
    ZSTM.die(t)

  /**
   * Kills the fiber running the effect with a `RuntimeException` that contains
   * the specified message.
   */
  def dieMessage(m: => String): STM[Nothing, Nothing] =
    ZSTM.dieMessage(m)

  /**
   * Returns a value modelled on provided exit status.
   */
  def done[E, A](exit: => ZSTM.internal.TExit[E, A]): STM[E, A] =
    ZSTM.done(exit)

  /**
   * Returns a value that models failure in the transaction.
   */
  def fail[E](e: => E): STM[E, Nothing] =
    ZSTM.fail(e)

  /**
   * Returns the fiber id of the fiber committing the transaction.
   */
  val fiberId: STM[Nothing, Fiber.Id] =
    ZSTM.fiberId

  /**
   * Applies the function `f` to each element of the `Iterable[A]` and
   * returns a transactional effect that produces a new `List[B]`.
   */
  def foreach[E, A, B](as: Iterable[A])(f: A => STM[E, B]): STM[E, List[B]] =
    ZSTM.foreach(as)(f)

  /**
   * Applies the function `f` to each element of the `Iterable[A]` and
   * returns a transactional effect that produces `Unit`.
   *
   * Equivalent to `foreach(as)(f).unit`, but without the cost of building
   * the list of results.
   */
  def foreach_[E, A, B](as: Iterable[A])(f: A => STM[E, B]): STM[E, Unit] =
    STM.succeedNow(as.iterator).flatMap { it =>
      def loop: STM[E, Unit] =
        if (it.hasNext) f(it.next) *> loop
        else STM.unit

      loop
    }

  /**
   * Creates an STM effect from an `Either` value.
   */
  def fromEither[E, A](e: => Either[E, A]): STM[E, A] =
    ZSTM.fromEither(e)

  /**
   * Creates an STM effect from a `Try` value.
   */
  def fromTry[A](a: => Try[A]): STM[Throwable, A] =
    ZSTM.fromTry(a)

  /**
   * Runs `onTrue` if the result of `b` is `true` and `onFalse` otherwise.
   */
  def ifM[E](b: STM[E, Boolean]): ZSTM.IfM[Any, E] =
    new ZSTM.IfM(b)

  /**
   * Iterates with the specified transactional function. The moral equivalent
   * of:
   *
   * {{{
   * var s = initial
   *
   * while (cont(s)) {
   *   s = body(s)
   * }
   *
   * s
   * }}}
   */
  def iterate[E, S](initial: S)(cont: S => Boolean)(body: S => STM[E, S]): STM[E, S] =
    ZSTM.iterate(initial)(cont)(body)

  /**
   * Loops with the specified transactional function, collecting the results
   * into a list. The moral equivalent of:
   *
   * {{{
   * var s  = initial
   * var as = List.empty[A]
   *
   * while (cont(s)) {
   *   as = body(s) :: as
   *   s  = inc(s)
   * }
   *
   * as.reverse
   * }}}
   */
  def loop[E, A, S](initial: S)(cont: S => Boolean, inc: S => S)(body: S => STM[E, A]): STM[E, List[A]] =
    ZSTM.loop(initial)(cont, inc)(body)

  /**
   * Loops with the specified transactional function purely for its
   * transactional effects. The moral equivalent of:
   *
   * {{{
   * var s = initial
   *
   * while (cont(s)) {
   *   body(s)
   *   s = inc(s)
   * }
   * }}}
   */
  def loop_[E, S](initial: S)(cont: S => Boolean, inc: S => S)(body: S => STM[E, Any]): STM[E, Unit] =
    ZSTM.loop_(initial)(cont, inc)(body)

  /**
   * Creates an `STM` value from a partial (but pure) function.
   */
  def partial[A](a: => A): STM[Throwable, A] =
    ZSTM.partial(a)

  /**
   * Abort and retry the whole transaction when any of the underlying
   * transactional variables have changed.
   */
  val retry: STM[Nothing, Nothing] =
    ZSTM.retry

  /**
   * Returns an `STM` effect that succeeds with the specified value.
   */
  def succeed[A](a: => A): STM[Nothing, A] =
    ZSTM.succeed(a)

  /**
   * Suspends creation of the specified transaction lazily.
   */
  def suspend[E, A](stm: => STM[E, A]): STM[E, A] =
    ZSTM.suspend(stm)

  /**
   * The moral equivalent of `if (p) exp`
   */
  def when[E](b: => Boolean)(stm: STM[E, Any]): STM[E, Unit] = ZSTM.when(b)(stm)

  /**
   * The moral equivalent of `if (p) exp` when `p` has side-effects
   */
  def whenM[E](b: STM[E, Boolean])(stm: STM[E, Any]): STM[E, Unit] = ZSTM.whenM(b)(stm)

  /**
   * Returns an `STM` effect that succeeds with `Unit`.
   */
  val unit: STM[Nothing, Unit] =
    ZSTM.unit

  private[zio] def dieNow(t: Throwable): STM[Nothing, Nothing] =
    ZSTM.dieNow(t)

  private[zio] def doneNow[E, A](exit: => ZSTM.internal.TExit[E, A]): STM[E, A] =
    ZSTM.doneNow(exit)

  private[zio] def failNow[E](e: E): STM[E, Nothing] =
    ZSTM.failNow(e)

  private[zio] def succeedNow[A](a: A): STM[Nothing, A] =
    ZSTM.succeedNow(a)
}
