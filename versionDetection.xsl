<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:project="http://maven.apache.org/POM/4.0.0" >

  <xsl:output method="text"/>

  <xsl:template match="/">
      <xsl:text>maven.groupId=</xsl:text>
      <xsl:value-of select="//project:groupId" />
      <xsl:text>&#10;</xsl:text>
      <xsl:text>maven.artifactId=</xsl:text>
      <xsl:value-of select="//project:artifactId" />
      <xsl:text>&#10;</xsl:text>
      <xsl:text>maven.version=</xsl:text>
      <xsl:value-of select="//project:version" />
      <xsl:text>&#10;</xsl:text>

      <xsl:text>maven.new.release.version=</xsl:text>
      <xsl:value-of select="substring-before(//project:version,'-SNAPSHOT')" />
      <xsl:text>&#10;</xsl:text>
      <xsl:text>maven.new.snapshot.version=</xsl:text>
      <xsl:call-template name="increment-version">
          <xsl:with-param name="text" select="substring-before(//project:version,'-SNAPSHOT')" />
      </xsl:call-template>
      <xsl:text>-SNAPSHOT&#10;</xsl:text>
  </xsl:template>

  <xsl:template name="increment-version">
    <xsl:param name="text" />
    <xsl:choose>
      <xsl:when test="contains($text, '.')">
        <xsl:value-of select="substring-before($text,'.')" />
        <xsl:text>.</xsl:text>
        <xsl:call-template name="increment-version">
          <xsl:with-param name="text" select="substring-after($text,'.')" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number($text)+1" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>