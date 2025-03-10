# JBang MCP Examples

```bash
./mvnw clean verify

jbang ./src/main/java/MCPCalculator.java
jbang ./src/main/java/MCPRealWeather.java

jbang mcp-calculator@jabrena
jbang mcp-realweather@jabrena

docker build -t mcp-calculator -f Dockerfile .

docker compose up -d
docker compose down
docker compose logs mcp-calculator

./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates
./mvnw versions:display-property-updates
```

## References

- https://github.com/quarkiverse/quarkus-mcp-servers/blob/main/jdbc/README.md
- https://github.com/punkpeye/awesome-mcp-servers
- https://github.com/jbangdev/jbang-action

Powered by [Cursor](https://www.cursor.com/)