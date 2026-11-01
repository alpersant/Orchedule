/// Spanish-friendly, case/accent-insensitive natural order: Equipo 2 before Equipo 10.
/// No external locale package is required. IDs are used by callers for stable ties.
String _teamKey(String value) {
  var result=value.trim().toLowerCase();
  const replacements={'á':'a','à':'a','ä':'a','â':'a','é':'e','è':'e','ë':'e','ê':'e',
    'í':'i','ì':'i','ï':'i','î':'i','ó':'o','ò':'o','ö':'o','ô':'o',
    'ú':'u','ù':'u','ü':'u','û':'u','ñ':'n~'};
  replacements.forEach((a,b)=>result=result.replaceAll(a,b));
  result=result.replaceAll('n\u0303','n~');
  return result.replaceAll(RegExp(r'[\u0300-\u036f]'),'');
}
int compareTeamNames(String a,String b){
  final x=RegExp(r'\d+|\D+').allMatches(_teamKey(a)).map((m)=>m.group(0)!).toList();
  final y=RegExp(r'\d+|\D+').allMatches(_teamKey(b)).map((m)=>m.group(0)!).toList();
  for(var i=0;i<x.length&&i<y.length;i++){
    final digits=RegExp(r'^\d+$');
    int order;
    if(digits.hasMatch(x[i])&&digits.hasMatch(y[i])){
      final nx=x[i].replaceFirst(RegExp(r'^0+(?=\d)'),''),ny=y[i].replaceFirst(RegExp(r'^0+(?=\d)'),'');
      order=nx.length.compareTo(ny.length);
      if(order==0)order=nx.compareTo(ny);
      if(order==0)order=x[i].length.compareTo(y[i].length);
    }else{order=x[i].compareTo(y[i]);}
    if(order!=0)return order;
  }
  return x.length.compareTo(y.length);
}
List<Map<String,dynamic>> orderedTeams(Iterable<Map<String,dynamic>> input){
  final unique=<String,Map<String,dynamic>>{};
  for(final row in input){
    final id=row['id'];
    if(id is! String||id.trim().isEmpty)throw const FormatException('Un equipo no tiene identificador válido. Actualiza los datos.');
    final name=row['name'];
    if(name is! String||name.trim().isEmpty)throw const FormatException('Un equipo no tiene nombre válido. Actualiza los datos.');
    unique.putIfAbsent(id,()=>Map<String,dynamic>.from(row));
  }
  return unique.values.toList()..sort((a,b){
    final result=compareTeamNames(a['name'] as String,b['name'] as String);
    return result==0?(a['id'] as String).compareTo(b['id'] as String):result;
  });
}
