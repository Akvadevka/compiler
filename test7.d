func checkValue(a) is
  if not a then
    return "Value is false";
  else
    return "Value is true";
  end
end

var num := readInt();
var text := readString();
var result := checkValue(num > 0);
print result, text;
