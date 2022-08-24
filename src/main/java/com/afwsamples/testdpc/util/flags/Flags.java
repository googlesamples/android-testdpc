/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.afwsamples.testdpc.util.flags;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Command registration methods are replicated 16 times to ensure parameter types match the function
// signature for compile-time type safety.
/**
 * Stores a list of registered commands and their parameters to later handle execution from the
 * command-line.
 */
public final class Flags {
  private static class InvalidCommandInvocationException extends RuntimeException {
    private final String message;

    InvalidCommandInvocationException(String message) {
      this.message = message;
    }

    public String message() {
      return message;
    }
  }

  private static RuntimeException commandInvocationException(String message, Object... args) {
    return new InvalidCommandInvocationException(String.format(message, args));
  }

  private static RuntimeException usageException(String message, Object... args) {
    return new UnsupportedOperationException(String.format(message, args));
  }

  /** Represents a function that accepts one argument and produces a result. */
  private interface Function<T, R> {
    R apply(T t);
  }

  /** Represents a function that accepts two arguments and produces a result. */
  public interface BiFunction<T1, T2, R> {
    R apply(T1 t1, T2 t2);
  }

  interface Param<T> {
    boolean isOrdinal();

    boolean isOptional();

    boolean acceptsMultipleValues();

    Class<?> dataType();

    String name();
  }

  private static final class OrdinalParam<T> implements Param<T> {
    private final Class<T> dataType;
    private final String name;

    private OrdinalParam(Class<T> dataType, String name) {
      this.dataType = dataType;
      this.name = name;
    }

    @Override
    public boolean isOrdinal() {
      return true;
    }

    @Override
    public boolean isOptional() {
      return false;
    }

    @Override
    public boolean acceptsMultipleValues() {
      return false;
    }

    @Override
    public Class<?> dataType() {
      return dataType;
    }

    @Override
    public String name() {
      return name;
    }
  }

  private static final class NamedParam<T> implements Param<T> {
    private final Class<T> dataType;
    private final String name;

    private NamedParam(Class<T> dataType, String name) {
      this.dataType = dataType;
      this.name = name;
    }

    @Override
    public boolean isOrdinal() {
      return false;
    }

    @Override
    public boolean isOptional() {
      return false;
    }

    @Override
    public boolean acceptsMultipleValues() {
      return false;
    }

    @Override
    public Class<?> dataType() {
      return dataType;
    }

    @Override
    public String name() {
      return name;
    }
  }

  private static final class OptionalParam<T> implements Param<T> {
    private final NamedParam<T> innerParam;

    private OptionalParam(NamedParam<T> innerParam) {
      this.innerParam = innerParam;
    }

    @Override
    public boolean isOrdinal() {
      return false;
    }

    @Override
    public boolean isOptional() {
      return true;
    }

    @Override
    public boolean acceptsMultipleValues() {
      return false;
    }

    @Override
    public Class<?> dataType() {
      return innerParam.dataType();
    }

    @Override
    public String name() {
      return innerParam.name();
    }
  }

  private static final class RepeatedOrdinalParam<T> implements Param<T> {
    private final OrdinalParam<?> innerParam;

    private RepeatedOrdinalParam(OrdinalParam<?> innerParam) {
      this.innerParam = innerParam;
    }

    @Override
    public boolean isOrdinal() {
      return true;
    }

    @Override
    public boolean isOptional() {
      return false;
    }

    @Override
    public boolean acceptsMultipleValues() {
      return true;
    }

    @Override
    public Class<?> dataType() {
      return innerParam.dataType();
    }

    @Override
    public String name() {
      return innerParam.name();
    }
  }

  private static final class RepeatedNamedParam<T> implements Param<T> {
    private final NamedParam<?> innerParam;

    private RepeatedNamedParam(NamedParam<?> innerParam) {
      this.innerParam = innerParam;
    }

    @Override
    public boolean isOrdinal() {
      return false;
    }

    @Override
    public boolean isOptional() {
      return false;
    }

    @Override
    public boolean acceptsMultipleValues() {
      return true;
    }

    @Override
    public Class<?> dataType() {
      return innerParam.dataType();
    }

    @Override
    public String name() {
      return innerParam.name();
    }
  }

  private static final class Params {
    private final List<Param<?>> ordinalParams;
    private final Map<String, Param<?>> namedParams;

    private Params(List<Param<?>> ordinalParams, Map<String, Param<?>> namedParams) {
      this.ordinalParams = ordinalParams;
      this.namedParams = namedParams;
    }

    private static Params create(Iterable<Param<?>> params) {
      List<Param<?>> ordinalParams = new ArrayList<>();
      Map<String, Param<?>> namedParams = new LinkedHashMap<>();

      for (Param<?> param : params) {
        if (param.isOrdinal()) {
          ordinalParams.add(param);
        } else {
          namedParams.put(param.name(), param);
        }
      }

      return new Params(ordinalParams, namedParams);
    }

    private List<Param<?>> ordinalParams() {
      return ordinalParams;
    }

    private Map<String, Param<?>> namedParams() {
      return namedParams;
    }
  }

  interface CommandMethod {
    void execute(Flags flags, ArgsContainer args);
  }

  static final class RegisteredCommand {
    private final String name;
    private final String description;
    private final Params params;
    private final CommandMethod commandMethod;

    private RegisteredCommand(Builder builder) {
      this.name = builder.name;
      this.description = builder.description;
      this.params = builder.params;
      this.commandMethod = builder.commandMethod;
    }

    private String name() {
      return name;
    }

    private String description() {
      return description;
    }

    private Params params() {
      return params;
    }

    private CommandMethod commandMethod() {
      return commandMethod;
    }

    private static RegisteredCommand.Builder builder() {
      return new Builder().setDescription("");
    }

    public static final class Builder {
      private String name;
      private String description;
      private Params params;
      private CommandMethod commandMethod;

      private Builder setName(String value) {
        name = value;
        return this;
      }

      private Builder setParams(Params value) {
        params = value;
        return this;
      }

      private Builder setCommandMethod(CommandMethod value) {
        commandMethod = value;
        return this;
      }

      public Builder setDescription(String value) {
        description = value;
        return this;
      }

      private RegisteredCommand build() {
        return new RegisteredCommand(this);
      }
    }
  }

  /** Generates validation results for an argument parser. */
  public static final class Validator<T> {
    /** Represents a successful or unsuccessful argument parse result. */
    public static final class ValidationResult<T> {
      private final boolean isValid;
      private final String message;
      private final T value;

      private ValidationResult(T value) {
        isValid = true;
        message = null;
        this.value = value;
      }

      private ValidationResult() {
        isValid = false;
        message = null;
        value = null;
      }

      private ValidationResult(String message) {
        isValid = false;
        this.message = message;
        value = null;
      }

      private boolean isValid() {
        return isValid;
      }

      private boolean hasMessage() {
        return message != null;
      }

      private String message() {
        return message;
      }

      private T value() {
        return value;
      }
    }

    /** Create a successful argument parse result containing the given value. */
    public ValidationResult<T> valid(T value) {
      return new ValidationResult<>(value);
    }

    /**
     * Create an unsuccessful argument parse result with no error message.
     *
     * @see #invalid(String)
     */
    public ValidationResult<T> invalid() {
      return new ValidationResult<>();
    }

    /**
     * Create an unsuccessful argument parse result with the given error message, which will be
     * printed at the start of the help text produced when an invalid parse result is detected.
     *
     * @see #invalid()
     */
    public ValidationResult<T> invalid(String message) {
      return new ValidationResult<>(message);
    }
  }

  private static final class TypeParser {
    private final Map<Class<?>, ArgumentParser<?>> parsers;

    private TypeParser(Map<Class<?>, ArgumentParser<?>> parsers) {
      this.parsers = parsers;
    }

    // Forced by type parameters for external calls and validated by tests for internal calls
    @SuppressWarnings("unchecked")
    private <T> T parse(Param<T> param, String value) {
      ArgumentParser<T> parser = (ArgumentParser<T>) parsers.get(param.dataType());
      Validator.ValidationResult<T> result = parser.parserFunc().apply(value, parser.validator());

      if (!result.isValid()) {
        if (result.hasMessage()) {
          throw commandInvocationException(
              "Parameter '%s' could not be parsed as '%s'.\n%s",
              param.name(), param.dataType().getSimpleName(), result.message());
        }

        throw commandInvocationException(
            "Parameter '%s' could not be parsed as '%s'.",
            param.name(), param.dataType().getSimpleName());
      }

      return result.value();
    }
  }

  private static final class CommandLineParser {
    private final Map<Param<?>, Object> parsedParams = new HashMap<>();

    private final Set<Param<?>> parsedOrdinalParams = new HashSet<>();

    private final TypeParser typeParser;

    private final String[] args;

    private int currentIndex = 1;

    private CommandLineParser(TypeParser typeParser, String[] args) {
      this.typeParser = typeParser;
      this.args = args;
    }

    private ArgsContainer parse(Params params) {
      while (hasNextValue()) {
        String currentArg = peekNextValue();

        if (isNamedArg(currentArg)) {
          advance();

          String argName = namedArgValueToName(currentArg);

          if (!params.namedParams().containsKey(argName)) {
            throw commandInvocationException("Named argument '%s' does not exist.", argName);
          }

          Param<?> param = params.namedParams().get(argName);

          if (parsedParams.containsKey(param)) {
            throw commandInvocationException("Named argument '%s' was called repeatedly.", argName);
          }

          if (param.acceptsMultipleValues()) {
            parseMultipleValues(param);
          } else {
            if (!hasNextValue()) {
              throw commandInvocationException(
                  "Expected value for parameter '%s' but it was not provided.", argName);
            }

            parseSingleValue(param, getNextValue());
          }
        } else {
          if (!hasOrdinalParamForCurrentArg(params)) {
            throw commandInvocationException(
                "More ordinal parameters were provided than the command accepts.");
          }

          Param<?> param = getCurrentOrdinalParam(params);

          if (param.acceptsMultipleValues()) {
            parseMultipleValues(param);
          } else {
            parseSingleValue(param, currentArg);
          }

          parsedOrdinalParams.add(param);

          advance();
        }
      }

      if (parsedOrdinalParams.size() < params.ordinalParams().size()) {
        for (Param<?> param : params.ordinalParams()) {
          if (!parsedOrdinalParams.contains(param) && !param.acceptsMultipleValues()) {
            throw commandInvocationException(
                "Ordinal parameter '%s' was not provided.", param.name());
          }
        }
      }

      int parsedNamedParamsCount = parsedParams.size() - parsedOrdinalParams.size();
      if (parsedNamedParamsCount < params.namedParams().size()) {
        for (Param<?> param : params.namedParams().values()) {
          if (!parsedParams.containsKey(param)
              && !param.isOptional()
              && !param.acceptsMultipleValues()) {
            throw commandInvocationException(
                "Named parameter '%s' was not provided.", param.name());
          }
        }
      }

      return new ArgsContainer(parsedParams);
    }

    private void advance() {
      ++currentIndex;
    }

    private boolean hasNextValue() {
      return currentIndex < args.length;
    }

    private String getNextValue() {
      return args[currentIndex++];
    }

    private String peekNextValue() {
      return args[currentIndex];
    }

    private boolean hasOrdinalParamForCurrentArg(Params params) {
      return parsedOrdinalParams.size() < params.ordinalParams().size();
    }

    private Param<?> getCurrentOrdinalParam(Params params) {
      return params.ordinalParams().get(parsedOrdinalParams.size());
    }

    private void parseSingleValue(Param<?> param, String value) {
      parsedParams.put(param, typeParser.parse(param, value));
    }

    private void parseMultipleValues(Param<?> param) {
      List<Object> value = new ArrayList<>();

      while (hasNextValue() && !isNamedArg(peekNextValue())) {
        value.add(typeParser.parse(param, getNextValue()));
      }

      parsedParams.put(param, asArray(param.dataType(), value));
    }

    private static boolean isNamedArg(String value) {
      return value.startsWith("--");
    }

    private static String namedArgValueToName(String arg) {
      return arg.substring("--".length());
    }

    private static Object asArray(Class<?> type, List<Object> values) {
      Object array = Array.newInstance(type, values.size());

      for (int index = 0; index < values.size(); ++index) {
        Array.set(array, index, values.get(index));
      }

      return array;
    }
  }

  private static final class ArgsContainer {
    private static final Map<Class<?>, Object> primitiveTypeDefaultValues = new HashMap<>();

    static {
      primitiveTypeDefaultValues.put(boolean.class, false);
      primitiveTypeDefaultValues.put(char.class, '\0');
      primitiveTypeDefaultValues.put(float.class, 0f);
      primitiveTypeDefaultValues.put(double.class, 0d);
      primitiveTypeDefaultValues.put(byte.class, (byte) 0);
      primitiveTypeDefaultValues.put(short.class, (short) 0);
      primitiveTypeDefaultValues.put(int.class, 0);
      primitiveTypeDefaultValues.put(long.class, 0L);
    }

    private final Map<Param<?>, Object> args;

    private ArgsContainer(Map<Param<?>, Object> args) {
      this.args = args;
    }

    private Object get(Param<?> param) {
      Object value = args.get(param);

      if (value == null) {
        if (param.acceptsMultipleValues()) {
          return Array.newInstance(param.dataType(), 0);
        }

        if (param.dataType().isPrimitive()) {
          return primitiveTypeDefaultValues.get(param.dataType());
        }
      }

      return value;
    }
  }

  private static final class ArgumentParser<T> {
    private final BiFunction<String, Validator<T>, Validator.ValidationResult<T>> parserFunc;
    private final Validator<T> validator;

    private ArgumentParser(
        BiFunction<String, Validator<T>, Validator.ValidationResult<T>> parserFunc,
        Validator<T> validator) {
      this.parserFunc = parserFunc;
      this.validator = validator;
    }

    private static <T> ArgumentParser<T> create(
        BiFunction<String, Validator<T>, Validator.ValidationResult<T>> parserFunc) {
      return new ArgumentParser<>(parserFunc, new Validator<T>());
    }

    private BiFunction<String, Validator<T>, Validator.ValidationResult<T>> parserFunc() {
      return parserFunc;
    }

    private Validator<T> validator() {
      return validator;
    }
  }

  private final PrintWriter printWriter;
  private final Map<Class<?>, ArgumentParser<?>> parsers = new HashMap<>();
  private final Map<String, RegisteredCommand> commands = new LinkedHashMap<>();

  /**
   * Creates a Flags instance that prints usage and error messages to the {@link System#out} stream.
   */
  public Flags() {
    this(new PrintWriter(System.out));
  }

  /**
   * Creates a Flags instance that prints usage and error messages to the specified {@code
   * PrintWriter}.
   */
  public Flags(PrintWriter printWriter) {
    this.printWriter = printWriter;
    registerDefaultArgumentParsers();
    addCommand(
        command("help", () -> usagePrinter().showUsage()).setDescription("Prints this help text."));
  }

  /**
   * Registers an argument parser for a custom type (or overrides default parsers for simple types).
   *
   * @param type The type to register a parser for.
   * @param parserFunc A function which tries to parse a string into the appropriate type, informing
   *     the validator of whether this was successful.
   */
  public <T> void registerCustomParser(
      Class<T> type, BiFunction<String, Validator<T>, Validator.ValidationResult<T>> parserFunc) {
    parsers.put(type, ArgumentParser.create(parserFunc));
  }

  /**
   * Runs the registered commands and their parameters against the provided command-line arguments.
   */
  public void run(String[] args) {
    if ((args == null) || (args.length == 0)) {
      usagePrinter().showUsage();
      return;
    }

    try {
      execute(args);
    } catch (InvalidCommandInvocationException e) {
      usagePrinter().showUsage(e.message());
    }
  }

  private UsagePrinter usagePrinter() {
    return new UsagePrinter(printWriter, commands.values());
  }

  private void execute(String[] args) {
    String commandName = args[0];

    if (!commands.containsKey(commandName)) {
      throw commandInvocationException("Command '%s' was not found.", commandName);
    }

    RegisteredCommand command = commands.get(commandName);
    command
        .commandMethod()
        .execute(
            this, new CommandLineParser(new TypeParser(parsers), args).parse(command.params()));
  }

  // All parser functions must provide the correct type - verified by unit tests and type
  // constraints.
  @SuppressWarnings("unchecked")
  private <T> T retrieveArg(Param<T> param, ArgsContainer args) {
    return (T) args.get(param);
  }

  private static final class UsagePrinter {
    private static final class LineLengthFormatter {
      private static final int LINE_LENGTH_LIMIT_CHARS = 80;

      private final String paddingOnAdditionalLine;
      private final String lineWrapSeparator;

      private LineLengthFormatter(String paddingOnAdditionalLine, String lineWrapSeparator) {
        this.paddingOnAdditionalLine = paddingOnAdditionalLine;
        this.lineWrapSeparator = lineWrapSeparator;
      }

      private void printText(PrintWriter printWriter, StringBuilder text) {
        int textLength = text.length();

        if (textLength > LINE_LENGTH_LIMIT_CHARS) {
          int textLastCharIndex = textLength - 1;
          int lineLastCharIndex = LINE_LENGTH_LIMIT_CHARS - 1;

          int currentLineStartIndex = 0;
          int currentLineEndIndex = lineLastCharIndex;

          StringBuilder multiLineBuilder = new StringBuilder();

          while (currentLineStartIndex < textLength) {
            int cutOffIndex = -1;

            if (currentLineEndIndex >= textLastCharIndex) {
              cutOffIndex = textLength;
            } else {
              currentLineEndIndex -= lineWrapSeparator.length();

              for (int currentCharIndex = currentLineStartIndex;
                  currentCharIndex <= (currentLineEndIndex + 1);
                  ++currentCharIndex) {
                if (Character.isWhitespace(text.charAt(currentCharIndex))) {
                  cutOffIndex = currentCharIndex;
                }
              }

              if (cutOffIndex == -1) {
                for (int currentCharIndex = currentLineEndIndex;
                    currentCharIndex < textLength;
                    ++currentCharIndex) {
                  if (Character.isWhitespace(text.charAt(currentCharIndex))) {
                    cutOffIndex = currentCharIndex;
                    break;
                  }
                }
              }

              if (cutOffIndex == -1) {
                cutOffIndex = textLength;
              }
            }

            multiLineBuilder.append(text, currentLineStartIndex, cutOffIndex);

            currentLineStartIndex = cutOffIndex + 1;
            currentLineEndIndex =
                Math.min(
                    textLastCharIndex,
                    currentLineStartIndex + lineLastCharIndex - paddingOnAdditionalLine.length());

            if (cutOffIndex < textLastCharIndex) {
              multiLineBuilder.append(lineWrapSeparator);
              multiLineBuilder.append('\n');
              multiLineBuilder.append(paddingOnAdditionalLine);
            }
          }

          printWriter.println(multiLineBuilder);
        } else {
          printWriter.println(text);
        }
      }
    }

    private static final String COMMAND_PADDING = "  ";
    private static final String COMMAND_WRAP_PADDING = "        ";
    private static final String DESCRIPTION_PADDING = "    ";
    private static final String COMMAND_WRAP_SEPARATOR = " \\";

    private final PrintWriter printWriter;
    private final Iterable<RegisteredCommand> commands;

    private final StringBuilder lineBuilder = new StringBuilder();

    private UsagePrinter(PrintWriter printWriter, Iterable<RegisteredCommand> commands) {
      this.printWriter = printWriter;
      this.commands = commands;
    }

    private void showUsage(String message) {
      printWriter.println(message);
      printWriter.println();
      showUsage();
    }

    private void showUsage() {
      printWriter.println("Usage:");
      for (RegisteredCommand command : commands) {
        lineBuilder.append(COMMAND_PADDING);
        lineBuilder.append(command.name());
        for (Param<?> param : command.params().ordinalParams()) {
          appendFormat(" <%s>", param.name());
          if (param.acceptsMultipleValues()) {
            lineBuilder.append("...");
          }
        }
        for (Param<?> param : command.params().namedParams().values()) {
          lineBuilder.append(" ");
          if (param.isOptional()) {
            lineBuilder.append("[");
          }
          appendFormat("--%s <value>", param.name());
          if (param.acceptsMultipleValues()) {
            lineBuilder.append("...");
          }
          if (param.isOptional()) {
            lineBuilder.append("]");
          }
        }
        printCommandLine();

        if (command.description().length() > 0) {
          lineBuilder.append(DESCRIPTION_PADDING);
          lineBuilder.append(command.description());
          printDescriptionLine();
        }

        printWriter.println();
      }
      printWriter.flush();
    }

    private void appendFormat(String format, Object... args) {
      lineBuilder.append(String.format(format, args));
    }

    private void printCommandLine() {
      new LineLengthFormatter(COMMAND_WRAP_PADDING, COMMAND_WRAP_SEPARATOR)
          .printText(printWriter, lineBuilder);
      clearLine();
    }

    private void printDescriptionLine() {
      new LineLengthFormatter(DESCRIPTION_PADDING, /* lineWrapSeparator= */ "")
          .printText(printWriter, lineBuilder);
      clearLine();
    }

    private void clearLine() {
      lineBuilder.setLength(0);
    }
  }

  private void registerDefaultArgumentParsers() {
    ArgumentParser<?> booleanParser =
        ArgumentParser.create(
            (string, validator) -> {
              String lowercaseString = string.toLowerCase(Locale.getDefault());

              if (lowercaseString.equals("true")) {
                return validator.valid(true);
              }

              if (lowercaseString.equals("false")) {
                return validator.valid(false);
              }

              return validator.invalid();
            });
    parsers.put(boolean.class, booleanParser);
    parsers.put(Boolean.class, booleanParser);

    ArgumentParser<?> charParser =
        ArgumentParser.create(
            (string, validator) -> {
              if (string.length() == 1) {
                return validator.valid(string.charAt(0));
              }

              return validator.invalid();
            });
    parsers.put(char.class, charParser);
    parsers.put(Character.class, charParser);

    ArgumentParser<?> floatParser = numericParser(Float::parseFloat);
    parsers.put(float.class, floatParser);
    parsers.put(Float.class, floatParser);

    ArgumentParser<?> doubleParser = numericParser(Double::parseDouble);
    parsers.put(double.class, doubleParser);
    parsers.put(Double.class, doubleParser);

    ArgumentParser<?> byteParser = numericParser(Byte::parseByte);
    parsers.put(byte.class, byteParser);
    parsers.put(Byte.class, byteParser);

    ArgumentParser<?> shortParser = numericParser(Short::parseShort);
    parsers.put(short.class, shortParser);
    parsers.put(Short.class, shortParser);

    ArgumentParser<?> integerParser = numericParser(Integer::parseInt);
    parsers.put(int.class, integerParser);
    parsers.put(Integer.class, integerParser);

    ArgumentParser<?> longParser = numericParser(Long::parseLong);
    parsers.put(long.class, longParser);
    parsers.put(Long.class, longParser);

    parsers.put(
        String.class, ArgumentParser.create((string, validator) -> validator.valid(string)));
  }

  @SuppressWarnings("ReturnValueIgnored")
  private static <T> ArgumentParser<T> numericParser(Function<String, T> parsingFunc) {
    return ArgumentParser.create(
        (string, validator) -> {
          T value;

          try {
            value = parsingFunc.apply(string);
          } catch (NumberFormatException e) {
            return validator.invalid();
          }

          return validator.valid(value);
        });
  }

  /**
   * Creates an ordinal parameter for a command. Ordinal parameters are parsed from the command-line
   * based on their registration order.
   */
  public static <T> OrdinalParam<T> ordinalParam(Class<T> type, String name) {
    return new OrdinalParam<>(type, name);
  }

  /**
   * Creates a named parameter for a command.
   *
   * <p>Named arguments can be provided anywhere after a command name, in the format {@code
   * --param-name param-value}.
   */
  public static <T> NamedParam<T> namedParam(Class<T> type, String name) {
    return new NamedParam<>(type, name);
  }

  /**
   * Convert a named parameter to an optional named parameter.
   *
   * <p>A optional parameters can be omitted by the user, which will cause the command callback to
   * be invoked with a zero-value for that parameter. A zero value is null for reference types and 0
   * for any value types.
   */
  public static <T> OptionalParam<T> optional(NamedParam<T> namedParam) {
    return new OptionalParam<>(namedParam);
  }

  /**
   * Converts an ordinal parameter to a repeated ordinal parameter.
   *
   * <p>Repeated ordinal parameters must come at the end of the ordinal parameter list.
   *
   * <p>Repeated parameter values are provided as an array to the callback, which means the array
   * will be empty (but not null) when the user does not provide any values.
   */
  public static <T> RepeatedOrdinalParam<T[]> repeated(OrdinalParam<T> innerParam) {
    return new RepeatedOrdinalParam<>(innerParam);
  }

  /**
   * Converts a named parameter to a repeated named parameter.
   *
   * <p>Repeated named parameters are provided on the command-line with this syntax: {@code
   * --param-name value1 value2 value3}.
   *
   * <p>Repeated parameter values are provided as an array to the callback, which means the array
   * will be empty (but not null) when the user does not provide any values or does not invoke the
   * parameter at all.
   */
  public static <T> RepeatedNamedParam<T[]> repeated(NamedParam<T> innerParam) {
    return new RepeatedNamedParam<>(innerParam);
  }

  /**
   * Adds a command specification, generated by calling a {@link Flags#command} overload, to the
   * list of commands handled by this {@link Flags} instance.
   *
   * <p>Commands are first validated to ensure that:
   *
   * <p>- There are no duplicate command or parameter names;
   *
   * <p>- All parameter types have associated parsers;
   *
   * <p>- There is up to one repeated ordinal parameter that must come at the end of the ordinal
   * parameter list.
   *
   * @throws UnsupportedOperationException if the command fails validation.
   */
  public void addCommand(RegisteredCommand.Builder commandBuilder) {
    RegisteredCommand command = commandBuilder.build();
    validateCommand(command);
    commands.put(command.name(), command);
  }

  private void validateCommand(RegisteredCommand command) {
    if (commands.containsKey(command.name())) {
      throw usageException("Duplicate command name '%s'.", command.name());
    }

    List<Param<?>> ordinalParams = command.params().ordinalParams();
    Map<String, Param<?>> namedParams = command.params().namedParams();

    for (int index = 0; index < (ordinalParams.size() - 1); ++index) {
      Param<?> param = ordinalParams.get(index);

      if (param.acceptsMultipleValues()) {
        throw usageException(
            "Repeated ordinal parameters must be at the end of the ordinal parameter list.");
      }
    }

    Set<String> paramNames = new HashSet<>();
    for (Param<?> param : ordinalParams) {
      validateParam(param, paramNames);
    }
    for (Param<?> param : namedParams.values()) {
      validateParam(param, paramNames);
    }
  }

  private void validateParam(Param<?> param, Set<String> paramNames) {
    if (paramNames.contains(param.name())) {
      throw usageException("Duplicate parameter name '%s'.", param.name());
    }

    if (!parsers.containsKey(param.dataType())) {
      throw usageException(
          "No custom parser registered for data type '%s' of param '%s'.",
          param.dataType(), param.name());
    }

    paramNames.add(param.name());
  }

  public static RegisteredCommand.Builder command(String name, CommandCallback0Params command) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(Params.create(new ArrayList<>()))
        .setCommandMethod(new CommandMethodImpl0(command));
  }

  public static <T1> RegisteredCommand.Builder command(
      String name, CommandCallback1Params<T1> callback, Param<T1> param1) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(Params.create(Arrays.asList(param1)))
        .setCommandMethod(new CommandMethodImpl1<>(callback, param1));
  }

  public static <T1, T2> RegisteredCommand.Builder command(
      String name, CommandCallback2Params<T1, T2> callback, Param<T1> param1, Param<T2> param2) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(Params.create(Arrays.asList(param1, param2)))
        .setCommandMethod(new CommandMethodImpl2<>(callback, param1, param2));
  }

  public static <T1, T2, T3> RegisteredCommand.Builder command(
      String name,
      CommandCallback3Params<T1, T2, T3> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(Params.create(Arrays.asList(param1, param2, param3)))
        .setCommandMethod(new CommandMethodImpl3<>(callback, param1, param2, param3));
  }

  public static <T1, T2, T3, T4> RegisteredCommand.Builder command(
      String name,
      CommandCallback4Params<T1, T2, T3, T4> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(Params.create(Arrays.asList(param1, param2, param3, param4)))
        .setCommandMethod(new CommandMethodImpl4<>(callback, param1, param2, param3, param4));
  }

  public static <T1, T2, T3, T4, T5> RegisteredCommand.Builder command(
      String name,
      CommandCallback5Params<T1, T2, T3, T4, T5> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4,
      Param<T5> param5) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(Params.create(Arrays.asList(param1, param2, param3, param4, param5)))
        .setCommandMethod(
            new CommandMethodImpl5<>(callback, param1, param2, param3, param4, param5));
  }

  public static <T1, T2, T3, T4, T5, T6> RegisteredCommand.Builder command(
      String name,
      CommandCallback6Params<T1, T2, T3, T4, T5, T6> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4,
      Param<T5> param5,
      Param<T6> param6) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(Params.create(Arrays.asList(param1, param2, param3, param4, param5, param6)))
        .setCommandMethod(
            new CommandMethodImpl6<>(callback, param1, param2, param3, param4, param5, param6));
  }

  public static <T1, T2, T3, T4, T5, T6, T7> RegisteredCommand.Builder command(
      String name,
      CommandCallback7Params<T1, T2, T3, T4, T5, T6, T7> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4,
      Param<T5> param5,
      Param<T6> param6,
      Param<T7> param7) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(Arrays.asList(param1, param2, param3, param4, param5, param6, param7)))
        .setCommandMethod(
            new CommandMethodImpl7<>(
                callback, param1, param2, param3, param4, param5, param6, param7));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8> RegisteredCommand.Builder command(
      String name,
      CommandCallback8Params<T1, T2, T3, T4, T5, T6, T7, T8> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4,
      Param<T5> param5,
      Param<T6> param6,
      Param<T7> param7,
      Param<T8> param8) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(param1, param2, param3, param4, param5, param6, param7, param8)))
        .setCommandMethod(
            new CommandMethodImpl8<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> RegisteredCommand.Builder command(
      String name,
      CommandCallback9Params<T1, T2, T3, T4, T5, T6, T7, T8, T9> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4,
      Param<T5> param5,
      Param<T6> param6,
      Param<T7> param7,
      Param<T8> param8,
      Param<T9> param9) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9)))
        .setCommandMethod(
            new CommandMethodImpl9<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> RegisteredCommand.Builder command(
      String name,
      CommandCallback10Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4,
      Param<T5> param5,
      Param<T6> param6,
      Param<T7> param7,
      Param<T8> param8,
      Param<T9> param9,
      Param<T10> param10) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9,
                    param10)))
        .setCommandMethod(
            new CommandMethodImpl10<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9,
                param10));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> RegisteredCommand.Builder command(
      String name,
      CommandCallback11Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> callback,
      Param<T1> param1,
      Param<T2> param2,
      Param<T3> param3,
      Param<T4> param4,
      Param<T5> param5,
      Param<T6> param6,
      Param<T7> param7,
      Param<T8> param8,
      Param<T9> param9,
      Param<T10> param10,
      Param<T11> param11) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9, param10,
                    param11)))
        .setCommandMethod(
            new CommandMethodImpl11<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9,
                param10, param11));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
      RegisteredCommand.Builder command(
          String name,
          CommandCallback12Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> callback,
          Param<T1> param1,
          Param<T2> param2,
          Param<T3> param3,
          Param<T4> param4,
          Param<T5> param5,
          Param<T6> param6,
          Param<T7> param7,
          Param<T8> param8,
          Param<T9> param9,
          Param<T10> param10,
          Param<T11> param11,
          Param<T12> param12) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9, param10,
                    param11, param12)))
        .setCommandMethod(
            new CommandMethodImpl12<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9,
                param10, param11, param12));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
      RegisteredCommand.Builder command(
          String name,
          CommandCallback13Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> callback,
          Param<T1> param1,
          Param<T2> param2,
          Param<T3> param3,
          Param<T4> param4,
          Param<T5> param5,
          Param<T6> param6,
          Param<T7> param7,
          Param<T8> param8,
          Param<T9> param9,
          Param<T10> param10,
          Param<T11> param11,
          Param<T12> param12,
          Param<T13> param13) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9, param10,
                    param11, param12, param13)))
        .setCommandMethod(
            new CommandMethodImpl13<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9,
                param10, param11, param12, param13));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
      RegisteredCommand.Builder command(
          String name,
          CommandCallback14Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
              callback,
          Param<T1> param1,
          Param<T2> param2,
          Param<T3> param3,
          Param<T4> param4,
          Param<T5> param5,
          Param<T6> param6,
          Param<T7> param7,
          Param<T8> param8,
          Param<T9> param9,
          Param<T10> param10,
          Param<T11> param11,
          Param<T12> param12,
          Param<T13> param13,
          Param<T14> param14) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9, param10,
                    param11, param12, param13, param14)))
        .setCommandMethod(
            new CommandMethodImpl14<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9,
                param10, param11, param12, param13, param14));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
      RegisteredCommand.Builder command(
          String name,
          CommandCallback15Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
              callback,
          Param<T1> param1,
          Param<T2> param2,
          Param<T3> param3,
          Param<T4> param4,
          Param<T5> param5,
          Param<T6> param6,
          Param<T7> param7,
          Param<T8> param8,
          Param<T9> param9,
          Param<T10> param10,
          Param<T11> param11,
          Param<T12> param12,
          Param<T13> param13,
          Param<T14> param14,
          Param<T15> param15) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9, param10,
                    param11, param12, param13, param14, param15)))
        .setCommandMethod(
            new CommandMethodImpl15<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9,
                param10, param11, param12, param13, param14, param15));
  }

  public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
      RegisteredCommand.Builder command(
          String name,
          CommandCallback16Params<
                  T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
              callback,
          Param<T1> param1,
          Param<T2> param2,
          Param<T3> param3,
          Param<T4> param4,
          Param<T5> param5,
          Param<T6> param6,
          Param<T7> param7,
          Param<T8> param8,
          Param<T9> param9,
          Param<T10> param10,
          Param<T11> param11,
          Param<T12> param12,
          Param<T13> param13,
          Param<T14> param14,
          Param<T15> param15,
          Param<T16> param16) {
    return RegisteredCommand.builder()
        .setName(name)
        .setParams(
            Params.create(
                Arrays.asList(
                    param1, param2, param3, param4, param5, param6, param7, param8, param9, param10,
                    param11, param12, param13, param14, param15, param16)))
        .setCommandMethod(
            new CommandMethodImpl16<>(
                callback, param1, param2, param3, param4, param5, param6, param7, param8, param9,
                param10, param11, param12, param13, param14, param15, param16));
  }

  public interface CommandCallback0Params {
    void execute();
  }

  public interface CommandCallback1Params<T1> {
    void execute(T1 arg1);
  }

  public interface CommandCallback2Params<T1, T2> {
    void execute(T1 arg1, T2 arg2);
  }

  public interface CommandCallback3Params<T1, T2, T3> {
    void execute(T1 arg1, T2 arg2, T3 arg3);
  }

  public interface CommandCallback4Params<T1, T2, T3, T4> {
    void execute(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
  }

  public interface CommandCallback5Params<T1, T2, T3, T4, T5> {
    void execute(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5);
  }

  public interface CommandCallback6Params<T1, T2, T3, T4, T5, T6> {
    void execute(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6);
  }

  public interface CommandCallback7Params<T1, T2, T3, T4, T5, T6, T7> {
    void execute(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7);
  }

  public interface CommandCallback8Params<T1, T2, T3, T4, T5, T6, T7, T8> {
    void execute(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8);
  }

  public interface CommandCallback9Params<T1, T2, T3, T4, T5, T6, T7, T8, T9> {
    void execute(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9);
  }

  public interface CommandCallback10Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {
    void execute(
        T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5, T6 arg6, T7 arg7, T8 arg8, T9 arg9, T10 arg10);
  }

  public interface CommandCallback11Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> {
    void execute(
        T1 arg1,
        T2 arg2,
        T3 arg3,
        T4 arg4,
        T5 arg5,
        T6 arg6,
        T7 arg7,
        T8 arg8,
        T9 arg9,
        T10 arg10,
        T11 arg11);
  }

  public interface CommandCallback12Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> {
    void execute(
        T1 arg1,
        T2 arg2,
        T3 arg3,
        T4 arg4,
        T5 arg5,
        T6 arg6,
        T7 arg7,
        T8 arg8,
        T9 arg9,
        T10 arg10,
        T11 arg11,
        T12 arg12);
  }

  public interface CommandCallback13Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> {
    void execute(
        T1 arg1,
        T2 arg2,
        T3 arg3,
        T4 arg4,
        T5 arg5,
        T6 arg6,
        T7 arg7,
        T8 arg8,
        T9 arg9,
        T10 arg10,
        T11 arg11,
        T12 arg12,
        T13 arg13);
  }

  public interface CommandCallback14Params<
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> {
    void execute(
        T1 arg1,
        T2 arg2,
        T3 arg3,
        T4 arg4,
        T5 arg5,
        T6 arg6,
        T7 arg7,
        T8 arg8,
        T9 arg9,
        T10 arg10,
        T11 arg11,
        T12 arg12,
        T13 arg13,
        T14 arg14);
  }

  public interface CommandCallback15Params<
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> {
    void execute(
        T1 arg1,
        T2 arg2,
        T3 arg3,
        T4 arg4,
        T5 arg5,
        T6 arg6,
        T7 arg7,
        T8 arg8,
        T9 arg9,
        T10 arg10,
        T11 arg11,
        T12 arg12,
        T13 arg13,
        T14 arg14,
        T15 arg15);
  }

  public interface CommandCallback16Params<
      T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> {
    void execute(
        T1 arg1,
        T2 arg2,
        T3 arg3,
        T4 arg4,
        T5 arg5,
        T6 arg6,
        T7 arg7,
        T8 arg8,
        T9 arg9,
        T10 arg10,
        T11 arg11,
        T12 arg12,
        T13 arg13,
        T14 arg14,
        T15 arg15,
        T16 arg16);
  }

  private static final class CommandMethodImpl0 implements CommandMethod {
    private final CommandCallback0Params callback;

    private CommandMethodImpl0(CommandCallback0Params callback) {
      this.callback = callback;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      callback.execute();
    }
  }

  private static final class CommandMethodImpl1<T1> implements CommandMethod {
    private final CommandCallback1Params<T1> callback;

    private final Param<T1> param1;

    private CommandMethodImpl1(CommandCallback1Params<T1> callback, Param<T1> param1) {
      this.callback = callback;
      this.param1 = param1;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);

      callback.execute(arg1);
    }
  }

  private static final class CommandMethodImpl2<T1, T2> implements CommandMethod {
    private final CommandCallback2Params<T1, T2> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;

    private CommandMethodImpl2(
        CommandCallback2Params<T1, T2> callback, Param<T1> param1, Param<T2> param2) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);

      callback.execute(arg1, arg2);
    }
  }

  private static final class CommandMethodImpl3<T1, T2, T3> implements CommandMethod {
    private final CommandCallback3Params<T1, T2, T3> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;

    private CommandMethodImpl3(
        CommandCallback3Params<T1, T2, T3> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);

      callback.execute(arg1, arg2, arg3);
    }
  }

  private static final class CommandMethodImpl4<T1, T2, T3, T4> implements CommandMethod {
    private final CommandCallback4Params<T1, T2, T3, T4> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;

    private CommandMethodImpl4(
        CommandCallback4Params<T1, T2, T3, T4> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);

      callback.execute(arg1, arg2, arg3, arg4);
    }
  }

  private static final class CommandMethodImpl5<T1, T2, T3, T4, T5> implements CommandMethod {
    private final CommandCallback5Params<T1, T2, T3, T4, T5> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;

    private CommandMethodImpl5(
        CommandCallback5Params<T1, T2, T3, T4, T5> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5);
    }
  }

  private static final class CommandMethodImpl6<T1, T2, T3, T4, T5, T6> implements CommandMethod {
    private final CommandCallback6Params<T1, T2, T3, T4, T5, T6> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;

    private CommandMethodImpl6(
        CommandCallback6Params<T1, T2, T3, T4, T5, T6> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5, arg6);
    }
  }

  private static final class CommandMethodImpl7<T1, T2, T3, T4, T5, T6, T7>
      implements CommandMethod {
    private final CommandCallback7Params<T1, T2, T3, T4, T5, T6, T7> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;

    private CommandMethodImpl7(
        CommandCallback7Params<T1, T2, T3, T4, T5, T6, T7> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }
  }

  private static final class CommandMethodImpl8<T1, T2, T3, T4, T5, T6, T7, T8>
      implements CommandMethod {
    private final CommandCallback8Params<T1, T2, T3, T4, T5, T6, T7, T8> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;

    private CommandMethodImpl8(
        CommandCallback8Params<T1, T2, T3, T4, T5, T6, T7, T8> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    }
  }

  private static final class CommandMethodImpl9<T1, T2, T3, T4, T5, T6, T7, T8, T9>
      implements CommandMethod {
    private final CommandCallback9Params<T1, T2, T3, T4, T5, T6, T7, T8, T9> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;

    private CommandMethodImpl9(
        CommandCallback9Params<T1, T2, T3, T4, T5, T6, T7, T8, T9> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
    }
  }

  private static final class CommandMethodImpl10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10>
      implements CommandMethod {
    private final CommandCallback10Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;
    private final Param<T10> param10;

    private CommandMethodImpl10(
        CommandCallback10Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9,
        Param<T10> param10) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
      this.param10 = param10;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);
      T10 arg10 = flags.retrieveArg(param10, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
    }
  }

  private static final class CommandMethodImpl11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11>
      implements CommandMethod {
    private final CommandCallback11Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;
    private final Param<T10> param10;
    private final Param<T11> param11;

    private CommandMethodImpl11(
        CommandCallback11Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9,
        Param<T10> param10,
        Param<T11> param11) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
      this.param10 = param10;
      this.param11 = param11;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);
      T10 arg10 = flags.retrieveArg(param10, args);
      T11 arg11 = flags.retrieveArg(param11, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);
    }
  }

  private static final class CommandMethodImpl12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
      implements CommandMethod {
    private final CommandCallback12Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12>
        callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;
    private final Param<T10> param10;
    private final Param<T11> param11;
    private final Param<T12> param12;

    private CommandMethodImpl12(
        CommandCallback12Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9,
        Param<T10> param10,
        Param<T11> param11,
        Param<T12> param12) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
      this.param10 = param10;
      this.param11 = param11;
      this.param12 = param12;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);
      T10 arg10 = flags.retrieveArg(param10, args);
      T11 arg11 = flags.retrieveArg(param11, args);
      T12 arg12 = flags.retrieveArg(param12, args);

      callback.execute(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
    }
  }

  private static final class CommandMethodImpl13<
          T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
      implements CommandMethod {
    private final CommandCallback13Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13>
        callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;
    private final Param<T10> param10;
    private final Param<T11> param11;
    private final Param<T12> param12;
    private final Param<T13> param13;

    private CommandMethodImpl13(
        CommandCallback13Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9,
        Param<T10> param10,
        Param<T11> param11,
        Param<T12> param12,
        Param<T13> param13) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
      this.param10 = param10;
      this.param11 = param11;
      this.param12 = param12;
      this.param13 = param13;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);
      T10 arg10 = flags.retrieveArg(param10, args);
      T11 arg11 = flags.retrieveArg(param11, args);
      T12 arg12 = flags.retrieveArg(param12, args);
      T13 arg13 = flags.retrieveArg(param13, args);

      callback.execute(
          arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13);
    }
  }

  private static final class CommandMethodImpl14<
          T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
      implements CommandMethod {
    private final CommandCallback14Params<
            T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
        callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;
    private final Param<T10> param10;
    private final Param<T11> param11;
    private final Param<T12> param12;
    private final Param<T13> param13;
    private final Param<T14> param14;

    private CommandMethodImpl14(
        CommandCallback14Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14>
            callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9,
        Param<T10> param10,
        Param<T11> param11,
        Param<T12> param12,
        Param<T13> param13,
        Param<T14> param14) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
      this.param10 = param10;
      this.param11 = param11;
      this.param12 = param12;
      this.param13 = param13;
      this.param14 = param14;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);
      T10 arg10 = flags.retrieveArg(param10, args);
      T11 arg11 = flags.retrieveArg(param11, args);
      T12 arg12 = flags.retrieveArg(param12, args);
      T13 arg13 = flags.retrieveArg(param13, args);
      T14 arg14 = flags.retrieveArg(param14, args);

      callback.execute(
          arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14);
    }
  }

  private static final class CommandMethodImpl15<
          T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
      implements CommandMethod {
    private final CommandCallback15Params<
            T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
        callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;
    private final Param<T10> param10;
    private final Param<T11> param11;
    private final Param<T12> param12;
    private final Param<T13> param13;
    private final Param<T14> param14;
    private final Param<T15> param15;

    private CommandMethodImpl15(
        CommandCallback15Params<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15>
            callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9,
        Param<T10> param10,
        Param<T11> param11,
        Param<T12> param12,
        Param<T13> param13,
        Param<T14> param14,
        Param<T15> param15) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
      this.param10 = param10;
      this.param11 = param11;
      this.param12 = param12;
      this.param13 = param13;
      this.param14 = param14;
      this.param15 = param15;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);
      T10 arg10 = flags.retrieveArg(param10, args);
      T11 arg11 = flags.retrieveArg(param11, args);
      T12 arg12 = flags.retrieveArg(param12, args);
      T13 arg13 = flags.retrieveArg(param13, args);
      T14 arg14 = flags.retrieveArg(param14, args);
      T15 arg15 = flags.retrieveArg(param15, args);

      callback.execute(
          arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14,
          arg15);
    }
  }

  private static final class CommandMethodImpl16<
          T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
      implements CommandMethod {
    private final CommandCallback16Params<
            T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
        callback;

    private final Param<T1> param1;
    private final Param<T2> param2;
    private final Param<T3> param3;
    private final Param<T4> param4;
    private final Param<T5> param5;
    private final Param<T6> param6;
    private final Param<T7> param7;
    private final Param<T8> param8;
    private final Param<T9> param9;
    private final Param<T10> param10;
    private final Param<T11> param11;
    private final Param<T12> param12;
    private final Param<T13> param13;
    private final Param<T14> param14;
    private final Param<T15> param15;
    private final Param<T16> param16;

    private CommandMethodImpl16(
        CommandCallback16Params<
                T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16>
            callback,
        Param<T1> param1,
        Param<T2> param2,
        Param<T3> param3,
        Param<T4> param4,
        Param<T5> param5,
        Param<T6> param6,
        Param<T7> param7,
        Param<T8> param8,
        Param<T9> param9,
        Param<T10> param10,
        Param<T11> param11,
        Param<T12> param12,
        Param<T13> param13,
        Param<T14> param14,
        Param<T15> param15,
        Param<T16> param16) {
      this.callback = callback;
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
      this.param6 = param6;
      this.param7 = param7;
      this.param8 = param8;
      this.param9 = param9;
      this.param10 = param10;
      this.param11 = param11;
      this.param12 = param12;
      this.param13 = param13;
      this.param14 = param14;
      this.param15 = param15;
      this.param16 = param16;
    }

    @Override
    public void execute(Flags flags, ArgsContainer args) {
      T1 arg1 = flags.retrieveArg(param1, args);
      T2 arg2 = flags.retrieveArg(param2, args);
      T3 arg3 = flags.retrieveArg(param3, args);
      T4 arg4 = flags.retrieveArg(param4, args);
      T5 arg5 = flags.retrieveArg(param5, args);
      T6 arg6 = flags.retrieveArg(param6, args);
      T7 arg7 = flags.retrieveArg(param7, args);
      T8 arg8 = flags.retrieveArg(param8, args);
      T9 arg9 = flags.retrieveArg(param9, args);
      T10 arg10 = flags.retrieveArg(param10, args);
      T11 arg11 = flags.retrieveArg(param11, args);
      T12 arg12 = flags.retrieveArg(param12, args);
      T13 arg13 = flags.retrieveArg(param13, args);
      T14 arg14 = flags.retrieveArg(param14, args);
      T15 arg15 = flags.retrieveArg(param15, args);
      T16 arg16 = flags.retrieveArg(param16, args);

      callback.execute(
          arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14,
          arg15, arg16);
    }
  }
}
