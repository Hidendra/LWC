# Permissions

Permissions in LWC v5 are handled by obeying each server mod's way of using Permissions for the most part. Permissions are stored internally as Bukkit/Spout noatation (some.node) so for Forge and Canary they are mapped to their ways of doing permissions.

<div align="center">
	<table>
		<thead>
			<tr>
				<th> Permission </th>
				<th> Bukkit </th>
				<th> Spout </th>
				<th> Forge </th>
				<th> Canary </th>
			</tr>
		</thead>
	
		<tbody>
			<tr>
				<th> LWC.PROTECT </th>
				<td> lwc.protect </td>
				<td> lwc.protect </td>
				<td> Anyone </td>
				<td> /lwc </td>
			</tr>
			
			<tr>
				<th> LWC.ADMIN </th>
				<td> lwc.admin </td>
				<td> lwc.admin </td>
				<td> OP </td>
				<td> /lwcadmin </td>
			</tr>
		</tbody>
	</table>
</div>