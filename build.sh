echo "*************************"
echo "building fake-web-tree"
echo "*************************"
(cd fake-web-tree; ./build.sh)
echo ""
echo "*************************"
echo "building golang"
echo "*************************"
(cd golang/web-crawler; ./build.sh)
echo ""
echo "*************************"
echo "building akka"
echo "*************************"
(cd akka/web-crawler; mvn install -DskipTests)
echo ""
echo "*************************"
echo "building java"
echo "*************************"
(cd java/web-crawler; mvn install -DskipTests)
echo ""
echo "*************************"
echo "building javaRx"
echo "*************************"
(cd javaRX/web-crawler; mvn install -DskipTests)
echo ""
echo "*************************"
echo "building nodejs"
echo ""
echo "*************************"
(cd nodejs; npm update)

echo ""
echo "Done."
echo ""

